package org.aksw.commons.beans.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class ReflectionUtils {

    /** Find a method or return null if none found. Never raises an exception. */
    public static Method findMethod(Class<?> clazz, String name, Class<?> ... parameterTypes) {
        Method result = null;
        try {
            result = clazz.getMethod(name, parameterTypes);
        } catch (Exception e) {
            // Ignore
        }
        return result;
    }


    /* Copy & adapted from springframework 5.2.8.RELEASE */
    public static void makeAccessible(Constructor<?> ctor) {
        if ((!Modifier.isPublic(ctor.getModifiers()) ||
                !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }

    /**
     * Copy & adapted from springframework 5.2.8.RELEASE
     *
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied {@code name} and/or {@link Class type}. Searches all superclasses
     * up to {@link Object}.
     * @param clazz the class to introspect
     * @param name the name of the field (may be {@code null} if type is specified)
     * @param type the type of the field (may be {@code null} if name is specified)
     * @return the corresponding Field object, or {@code null} if not found
     */
    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        Objects.requireNonNull(clazz, "Class must not be null");
        //Assert.assertTrue(name != null || type != null, "Either name or type of the field must be specified");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields;
            try {
                fields = searchType.getDeclaredFields(); //searchType);
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                        "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }

            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}
