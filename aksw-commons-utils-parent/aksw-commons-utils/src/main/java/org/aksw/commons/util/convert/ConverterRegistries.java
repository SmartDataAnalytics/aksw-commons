package org.aksw.commons.util.convert;

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
}
