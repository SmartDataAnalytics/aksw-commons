package org.aksw.commons.util.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;


public class ConvertFunctionRawImpl
    implements ConvertFunctionRaw
{
    protected Class<?> from;
    protected Class<?> to;
    protected Function<Object, Object> fn;

    public ConvertFunctionRawImpl(Class<?> from, Class<?> to, Function<Object, Object> fn) {
        super();
        this.from = from;
        this.to = to;
        this.fn = fn;
    }

    @Override
    public Class<?> getFrom() {
        return from;
    }

    @Override
    public Class<?> getTo() {
        return to;
    }

    @Override
    public Function<Object, Object> getRawFunction() {
        return fn;
    }


    /** Create a converter from a lambda */
    @SuppressWarnings("unchecked")
    public static <I, O> ConvertFunctionRaw create(
            Class<I> src,
            Class<O> tgt,
            Function<? super I, ? extends O> srcToTgt) {
        return new ConvertFunctionRawImpl(src, tgt, in -> srcToTgt.apply((I)in));
    }

    /** Create a converter from a method that takes a single argument and returns a non-void type */
    public static ConvertFunctionRaw create(Method method) {
        return create(method, null);
    }

    /** Create a converter from a method that takes a single argument and returns a non-void type */
    public static ConvertFunctionRaw create(Method method, Object obj) {
        Class<?> tgtType = method.getReturnType();

        if (Void.class.isAssignableFrom(tgtType)) {
            throw new IllegalArgumentException("Method must not return void; got: " + method);
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length != 1) {
            throw new IllegalArgumentException("Method must have exacly 1 argument; got: " + method);
        }

        Class<?> srcType = paramTypes[0];

        Function<Object, Object> fn = src -> {
            Method thisMethod = method; // Cached for debugging
            try {
                return thisMethod.invoke(obj, src);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        return new ConvertFunctionRawImpl(srcType, tgtType, fn);
    }
}

