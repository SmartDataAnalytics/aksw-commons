package org.aksw.commons.util.convert;

import java.lang.reflect.Method;
import java.util.function.Function;

public interface ConverterRegistry {

    ConvertFunctionRaw getConverter(Class<?> from, Class<?> to);
    void register(ConvertFunctionRaw converter);

    default <R, J> ConverterRegistry register(
            Class<R> src,
            Class<J> tgt,
            Function<? super R, ? extends J> srcToTgt) {
        ConvertFunctionRaw converter = ConvertFunctionRawImpl.create(src, tgt, srcToTgt);
        register(converter);

        return this;
    }

    default <R, J> ConverterRegistry register(
            Class<R> src,
            Class<J> tgt,
            Function<? super R, ? extends J> srcToTgt,
            Function<? super J, ? extends R> tgtToSrc) {
        register(src, tgt, srcToTgt);
        register(tgt, src, tgtToSrc);

        return this;
    }

    default ConverterRegistry register(Method method) {
        ConvertFunctionRaw converter = ConvertFunctionRawImpl.create(method);
        register(converter);

        return this;
    }
}
