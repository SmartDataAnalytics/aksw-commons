package org.aksw.commons.util.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.function.ThrowingRunnable;
import org.aksw.commons.util.traverse.BreadthFirstSearchLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static Stream<Class<?>> getDirectSuperclassAndInterfaces(Class<?> cls) {
        return Stream.concat(
                Optional.ofNullable(cls.getSuperclass()).stream(),
                Stream.of(cls.getInterfaces()));
    }

    /** Stream a classes super class and interfaces as lists of breadths */
    public static Stream<List<Class<?>>> bfsStream(Class<?> start) {
        return BreadthFirstSearchLib.stream(
                Collections.singletonList(start),
                ClassUtils::getDirectSuperclassAndInterfaces,
                () -> Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValueChecked(Class<?> clazz, String fieldName, Object obj)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        Object result = accessCalc(field, () -> field.get(obj));
        return (T)result;
    }

    public static <T> T getFieldValue(Class<?> clazz, String fieldName, Object obj) {
        try {
            T result = getFieldValueChecked(clazz, fieldName, obj);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj.getClass(), fieldName, obj);
    }

    public static Field getModifiersField() throws IllegalAccessException, NoSuchFieldException {
        // this is copied from https://github.com/powermock/powermock/pull/1010/files to
        // work around
        // JDK 12+
        Field modifiersField = null;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                Field[] fields = accessCalc(getDeclaredFields0, () -> (Field[]) getDeclaredFields0.invoke(Field.class, false));
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        modifiersField = field;
                        break;
                    }
                }
                if (modifiersField == null) {
                    throw e;
                }
            } catch (NoSuchMethodException ex) {
                e.addSuppressed(ex);
                throw e;
            }
        }
        return modifiersField;
    }

    public static void setFieldValueChecked(
            Class<?> clazz,
            String fieldName,
            Object obj,
            Object value)
                throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Field field = clazz.getDeclaredField(fieldName);
        access(field, () -> {
            if ((field.getModifiers() & Modifier.FINAL) != 0) {
                Field modifiersField = getModifiersField();
                access(modifiersField, () -> {
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                });
            }

            field.set(obj, value);
        });
    }

    public static void access(AccessibleObject obj, ThrowingRunnable consumer) {
        boolean isAccessible = obj.isAccessible();
        if (!isAccessible) {
            obj.setAccessible(true);
        }

        try {
            consumer.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!isAccessible) {
                obj.setAccessible(false);
            }
        }
    }

    public static <T> T accessCalc(AccessibleObject obj, Callable<T> consumer) {
        boolean isAccessible = obj.isAccessible();
        if (!isAccessible) {
            obj.setAccessible(true);
        }

        try {
            T result = consumer.call();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!isAccessible) {
                obj.setAccessible(false);
            }
        }
    }

    public static void setFieldValue(
            Class<?> clazz,
            String fieldName,
            Object obj,
            Object value) {
        try {
            setFieldValueChecked(clazz, fieldName, obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(
            Object obj,
            String fieldName,
            Object value) {
        setFieldValue(obj.getClass(), fieldName, null, value);
    }

    public static Object forceInvoke(Method m, Object[] args)
    {
        return forceInvoke(null, m, args);
    }

    public static Object forceInvoke(Object o, Method m, Object... args)
    {
        // FIXME Not multithreading safe
        return accessCalc(m, () -> m.invoke(o, args));
    }

    /**
     * Returns the minimum distance of two classes in an inheritance hierarchy
     * Null if there is no distance
     */
    public static Integer getDistance(Class<?> given, Class<?> there)
    {
        int result;
        if(there.isInterface()) {
            result = _getDistanceInterface(given, there, 0);
        } else {
            result = _getDistanceClass(given, there);
        }

        return result == Integer.MAX_VALUE ? null : result;
    }

    private static int _getDistanceClass(Class<?> given, Class<?> there)
    {
        int distance = 0;
        do {
            if(given == there) {
                return distance;
            }

            distance += 1;
            given = given.getSuperclass();


        } while (given != null);

        return Integer.MAX_VALUE;
    }

    private static int _getDistanceInterface(Class<?> given, Class<?> there, int depth)
    {
        if(given == there) {
            return depth;
        }

        ++depth;

        int result = Integer.MAX_VALUE;
        for(Class<?> item : given.getInterfaces()) {
            result = Math.min(result, _getDistanceInterface(item, there, depth));
        }

        Class<?> superClass = given.getSuperclass();
        if(superClass != null) {
            result = Math.min(result, _getDistanceInterface(superClass, there, depth));
        }

        return result;
    }

    public static List<Class<?>> getTypeSignatureList(Object[] args)
    {
        List<Class<?>> result = new ArrayList<Class<?>>(args.length);
        for(int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            result.add(arg == null ? null : arg.getClass());
        }

        return result;
    }

    public static Class<?>[] getTypeSignature(Object[] args)
    {
        Class<?>[] result = new Class<?>[args.length];
        for(int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            result[i] = (arg == null ? null : arg.getClass());
        }

        return result;
    }

    public static Integer[] getDistance(Class<?>[] a, Class<?>[] b)
    {
        int n = Math.min(a.length, b.length);

        Integer[] result = new Integer[n];
        for(int i = 0; i < n; ++i) {
            Class<?> given = a[i];


            // Don't try to abbreviate with (given == null) ? 0 : getDistance(given, b[i]);
            // It will break because getDistance may return null
            if(given == null) {
                result[i] = 0;
            } else {
                result[i] = getDistance(given, b[i]);
            }
        }

        return  result;
    }

    /**
     * Including return types
     *
     * @param ra
     * @param rb
     * @param a
     * @param b
     * @return
     */
    public static Integer[] getDistance(Class<?> ra, Class<?> rb, Class<?>[] a, Class<?>[] b)
    {
        int n = Math.min(a.length, b.length);

        Integer[] result = new Integer[n + 1];
        result[0] = getDistance(rb, ra);

        for(int i = 0; i < n; ++i) {
            Integer d = getDistance(a[i], b[i]);
            result[i + 1] = d;
        }

        return  result;
    }


    /*
    public static Integer getRelation(Integer a, Integer b) {
        if(a == null || b == null) {
            return null;
        }

        Integer result = a - b;
        return result;
    }
    */

    /**
     *
     *
     * @param a
     * @param b
     * @return L, LE, E, GE, G, Mixed
     */
    public static Integer getRelation(Integer[] a, Integer[] b)
    {
        boolean hasGreater = false;
        boolean hasLess = false;

        for(int i = 0; i < a.length; ++i) {
            if(a[i] == null || b[i] == null) {
                //return null;
                throw new NullPointerException();
                // TODO Throw an exception or return null?
            }

            int d = a[i] - b[i];

            if (d > 0) {
                hasGreater = true;
            } else if (d < 0) {
                hasLess = true;
            }
        }

        if(hasGreater && hasLess) {
            return null;
        } else if(hasGreater) {
            return 1;
        } else if(hasLess) {
            return -1;
        }

        return 0;
    }

    /**
     * Returns all non-overridden methods for the given class.
     *
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAllNonOverriddenMethods(Class<?> clazz)
    {
        List<Method> result = getAllNonOverriddenMethods(clazz, null);
        return result;
    }

    /**
     *
     *
     * @param clazz
     * @param name Convenience filter by name
     * @return
     */
    public static List<Method> getAllNonOverriddenMethods(Class<?> clazz, String name)
    {
        List<Method> result = new ArrayList<Method>();

        Set<MethodSignature> signatures = new HashSet<MethodSignature>();
        while(clazz != null) {
            for(Method method : clazz.getDeclaredMethods()) {

                if(name != null && !method.getName().equals(name)) {
                    continue;
                }

                MethodSignature signature = new MethodSignature(method);

                if(!signatures.contains(signature)) {
                    result.add(method);

                    signatures.add(signature);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return result;
    }

    public static Set<Class<?>> getMostSpecificSubclasses(Class<?> given, Collection<Class<?>> classes) {
        // Filter the set by all classes that are a subclass of the given one
        Set<Class<?>> tmp = classes.stream()
            .filter(given::isAssignableFrom)
            .collect(Collectors.toSet());

        Set<Class<?>> result = getNonSubsumedClasses(tmp);
        return result;
    }


    public static Set<Class<?>> getNonSubsumedClasses(Collection<Class<?>> classes) {
        // Retain all classes which are not a super class of any other
        Set<Class<?>> result = classes.stream()
            .filter(c -> classes.stream()
                    .filter(d -> !c.equals(d)) // Do not compare classes to itself
                    .noneMatch(c::isAssignableFrom))
            .collect(Collectors.toSet());

        return result;
    }


    /**
     * Return a supplier that invokes the given class' no-arg constructor.
     *
     * @param cls The class
     * @param createTestInstance If true then for verification an instance is
     *          taken from the created supplier immediately.
     */
    public static <T> Supplier<T> supplierFromCtor(Class<?> cls, boolean createTestInstance) {
        Constructor<?> ctor;
        try {
            ctor = cls.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        return supplierFromCtor(ctor, createTestInstance);
    }

    /**
     * Return a supplier that invokes the constructor.
     *
     * @param ctor The constructor
     * @param createTestInstance If true then for verification an instance is
     *          taken from the created supplier immediately.
     */
    public static <T> Supplier<T> supplierFromCtor(Constructor<?> ctor, boolean createTestInstance) {

        Supplier<T> result = () -> {
            Object obj;
            try {
                obj = ctor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            @SuppressWarnings("unchecked")
            T r = (T)obj;
            return r;
        };

        if (createTestInstance) {
            T test = result.get();
        }

        return result;
    }
}
