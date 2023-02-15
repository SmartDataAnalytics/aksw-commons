package org.aksw.commons.util.convert;

import java.math.BigDecimal;

import com.google.common.primitives.Primitives;

public class ConverterRegistries {

    /** If the initial lookup fails and any argument is a primitive type then retry with boxed types */
    public static ConvertFunctionRaw getConverterBoxed(ConverterRegistry registry, Class<?> from, Class<?> to) {
        ConvertFunctionRaw result = registry.getConverter(from, to);

        // If there was no result then try again with boxed types
        if (result == null) {
            Class<?> wrappedFrom = Primitives.wrap(from);
            Class<?> wrappedTo = Primitives.wrap(to);

            if (wrappedFrom != from || wrappedTo != to) {
                result = registry.getConverter(wrappedFrom, wrappedTo);
            }
        }

        return result;
    }

    private static ConverterRegistry DFT_REGISTRY = null;

    public static ConverterRegistry getDefaultConverterRegistry() {
        if (DFT_REGISTRY == null) {
            synchronized (ConverterRegistry.class) {
                if (DFT_REGISTRY == null) {
                    DFT_REGISTRY = new ConverterRegistryImpl();
                    addDefaultConversions(DFT_REGISTRY);
                }
            }
        }
        return DFT_REGISTRY;
    }

    public static Object convert(ConverterRegistry converterRegistry, Object input, Class<?> target) {
        Object result;

        if (input == null) {
            result = null;
        } else {
            Class<?> inputClass = input.getClass();

            // This method takes into account widenings of primitive classes
            if (org.apache.commons.lang3.ClassUtils.isAssignable(inputClass, target)) {
                result = input;
            } else {
                // ConvertFunctionRaw converter = converterRegistry.getConverter(inputClass, target);
                ConvertFunctionRaw converter = ConverterRegistries.getConverterBoxed(converterRegistry, inputClass, target);

                if (converter == null) {
                    throw new RuntimeException(String.format("No converter registered from %1$s to %2$s", inputClass, target));
                }

                result = converter.convertRaw(input);
            }
        }

        return result;
    }

    public static void addDefaultConversions(ConverterRegistry registry) {
        registry
            .register(BigDecimal.class, Long.class,
                    BigDecimal::longValueExact, BigDecimal::new)
            .register(BigDecimal.class, Integer.class,
                    BigDecimal::intValueExact, BigDecimal::new)
            .register(BigDecimal.class, Short.class,
                    BigDecimal::shortValueExact, BigDecimal::new)
            .register(BigDecimal.class, Byte.class,
                    BigDecimal::byteValueExact, BigDecimal::new)
            .register(BigDecimal.class, Double.class,
                    BigDecimal::doubleValue, BigDecimal::new)
            .register(BigDecimal.class, Float.class,
                    BigDecimal::floatValue, BigDecimal::new);
    }
}

