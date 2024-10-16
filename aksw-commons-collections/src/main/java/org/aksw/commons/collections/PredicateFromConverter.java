package org.aksw.commons.collections;

import java.util.function.Predicate;

import com.google.common.base.Converter;

public class PredicateFromConverter<T>
    implements Predicate<Object>
{
    protected Converter<T, ?> converter;

    public PredicateFromConverter(Converter<T, ?> converter) {
        super();
        this.converter = converter;
    }

    @Override
    public boolean test(Object raw) {
        boolean result;
        try {
            @SuppressWarnings("unchecked")
            T casted = (T)raw;
            converter.convert(casted);
            result = true;
        } catch(Exception e) {
            // TODO We want to forward some exceptions such as UnsupportedPolymorphismException - use a predicate to decide whether to raise an exception?
            result = false;
        }
        return result;
    }

    public static <T> Predicate<Object> create(Converter<T, ?> converter) {
        return new PredicateFromConverter<T>(converter);
    }
}
