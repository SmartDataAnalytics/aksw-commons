package org.aksw.commons.beans.model;

import java.util.function.BiPredicate;

/**
 * A generic adapter for ConversionService.
 * Allows to easily bridge to any external implementations such as those of the spring framework
 * as shown in the example below.
 *
 *
 * <pre>
 * DefaultConversionService dcs = new org.springframework.core.convert.support.DefaultConversionService();
 * org.aksw.commons.beans.model.ConversionService cs = ConversionServiceAdapter.wrap(dcs, dcs::canConvert, dcs:convert)
 * </pre>
 *
 * @author Claus Stadler
 *
 */
public class ConversionServiceAdapter
    implements ConversionService
{
    @FunctionalInterface
    public static interface ConvertFunction {
        <T> T convert(Object sourceValue, Class<T> targetType);
    }

    /** The delegating object such as a spring ConversionService - should be specified but may be null */
    protected Object delegate;
    protected BiPredicate<Class<?>, Class<?>> canConvert;
    protected ConvertFunction converter;

    public ConversionServiceAdapter(Object delegate, BiPredicate<Class<?>, Class<?>> canConvert,
            ConvertFunction converter) {
        super();
        this.delegate = delegate;
        this.canConvert = canConvert;
        this.converter = converter;
    }

    public Object getDelegate() {
        return delegate;
    }

    @Override
    public <T> boolean canConvert(Class<?> sourceType, Class<T> targetType) {
        boolean result = canConvert.test(sourceType, targetType);
        return result;
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        T result = converter.convert(source, targetType);
        return result;
    }

    public static ConversionService wrap(
            BiPredicate<Class<?>, Class<?>> canConvert,
            ConvertFunction convert) {
        return wrap(null, canConvert, convert);
    }

    public static ConversionService wrap(
            Object delegate,
            BiPredicate<Class<?>, Class<?>> canConvert,
            ConvertFunction convert) {
        return new ConversionServiceAdapter(delegate, canConvert, convert);
    }

}
