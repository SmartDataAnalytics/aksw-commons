package org.aksw.commons.util.convert;

import java.util.function.Function;

/** Interface to describe a conversion from one Java type to another */
public interface ConvertFunctionRaw {
    Class<?> getFrom();
    Class<?> getTo();
    Function<Object, Object> getRawFunction();

    default ConvertFunctionRaw andThen(ConvertFunctionRaw next) {
        Class<?> provided = getTo();
        Class<?> accepted = next.getFrom();

        if (!accepted.isAssignableFrom(provided)) {
            throw new RuntimeException(String.format("Cannot chain converters because the provided outgoing type %1$s is not accepted by %$2s",
                    provided, accepted));
        }

        Class<?> newSrc = getFrom();
        Class<?> newTgt = next.getTo();

        return new ConvertFunctionRawImpl(newSrc, newTgt, in -> {
            Object tmp = convertRaw(in);
            Object r = next.convertRaw(tmp);
            return r;
        });
    }

    default Object convertRaw(Object obj) {
        Function<Object, Object> fn = getRawFunction();
        Object result = fn.apply(obj);
        return result;
    }
}
