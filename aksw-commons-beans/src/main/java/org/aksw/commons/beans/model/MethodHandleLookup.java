package org.aksw.commons.beans.model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aksw.commons.util.memoize.MemoizedSupplier;
import org.aksw.commons.util.memoize.MemoizedSupplierImpl;

/** Copy/paste from https://github.com/spring-projects/spring-data-commons/blob/2.1.8.RELEASE/src/main/java/org/springframework/data/projection/DefaultMethodInvokingMethodInterceptor.java */
public enum MethodHandleLookup {

    /**
     * Encapsulated {@link MethodHandle} lookup working on Java 9.
     */
    ENCAPSULATED {

        private final Method privateLookupIn = ReflectionUtils.findMethod(MethodHandles.class,
                "privateLookupIn", Class.class, Lookup.class);

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#lookup(java.lang.reflect.Method)
         */
        @Override
        public MethodHandle lookup(Method method) throws ReflectiveOperationException {

            if (privateLookupIn == null) {
                throw new IllegalStateException("Could not obtain MethodHandles.privateLookupIn!");
            }

            return doLookup(method, getLookup(method.getDeclaringClass(), privateLookupIn));
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#isAvailable()
         */
        @Override
        public boolean isAvailable() {
            return privateLookupIn != null;
        }

        private Lookup getLookup(Class<?> declaringClass, Method privateLookupIn) {

            Lookup lookup = MethodHandles.lookup();

            try {
                return (Lookup) privateLookupIn.invoke(MethodHandles.class, declaringClass, lookup);
            } catch (ReflectiveOperationException e) {
                return lookup;
            }
        }
    },

    /**
     * Open (via reflection construction of {@link MethodHandles.Lookup}) method handle lookup. Works with Java 8 and
     * with Java 9 permitting illegal access.
     */
    OPEN {

        private final MemoizedSupplier<Constructor<Lookup>> constructor = MemoizedSupplierImpl.of(MethodHandleLookup::getLookupConstructor);

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#lookup(java.lang.reflect.Method)
         */
        @Override
        public MethodHandle lookup(Method method) throws ReflectiveOperationException {

            if (!isAvailable()) {
                throw new IllegalStateException("Could not obtain MethodHandles.lookup constructor!");
            }

            Constructor<Lookup> constructor = this.constructor.get();

            return constructor.newInstance(method.getDeclaringClass()).unreflectSpecial(method, method.getDeclaringClass());
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#isAvailable()
         */
        @Override
        public boolean isAvailable() {
            return constructor.get() != null;
        }
    },

    /**
     * Fallback {@link MethodHandle} lookup using {@link MethodHandles#lookup() public lookup}.
     *
     * @since 2.1
     */
    FALLBACK {

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#lookup(java.lang.reflect.Method)
         */
        @Override
        public MethodHandle lookup(Method method) throws ReflectiveOperationException {
            return doLookup(method, MethodHandles.lookup());
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.MethodHandleLookup#isAvailable()
         */
        @Override
        public boolean isAvailable() {
            return true;
        }
    };

    private static MethodHandle doLookup(Method method, Lookup lookup)
            throws NoSuchMethodException, IllegalAccessException {

        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());

        if (Modifier.isStatic(method.getModifiers())) {
            return lookup.findStatic(method.getDeclaringClass(), method.getName(), methodType);
        }

        return lookup.findSpecial(method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass());
    }

    /**
     * Lookup a {@link MethodHandle} given {@link Method} to look up.
     *
     * @param method must not be {@literal null}.
     * @return the method handle.
     * @throws ReflectiveOperationException
     */
    public abstract MethodHandle lookup(Method method) throws ReflectiveOperationException;

    /**
     * @return {@literal true} if the lookup is available.
     */
    public abstract boolean isAvailable();

    /**
     * Obtain the first available {@link MethodHandleLookup}.
     *
     * @return the {@link MethodHandleLookup}
     * @throws IllegalStateException if no {@link MethodHandleLookup} is available.
     */
    public static MethodHandleLookup getMethodHandleLookup() {

        for (MethodHandleLookup it : MethodHandleLookup.values()) {

            if (it.isAvailable()) {
                return it;
            }
        }

        throw new IllegalStateException("No MethodHandleLookup available!");
    }

    private static Constructor<Lookup> getLookupConstructor() {

        try {

            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            ReflectionUtils.makeAccessible(constructor);

            return constructor;
        } catch (Exception ex) {

            // this is the signal that we are on Java 9 (encapsulated) and can't use the accessible constructor approach.
            if (ex.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
                return null;
            }

            throw new IllegalStateException(ex);
        }
    }
}

