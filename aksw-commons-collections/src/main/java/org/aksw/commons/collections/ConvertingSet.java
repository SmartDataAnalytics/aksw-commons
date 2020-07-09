package org.aksw.commons.collections;

import java.util.Set;

import com.google.common.base.Converter;

public class ConvertingSet<F, B, C extends Set<B>>
    extends ConvertingCollection<F, B, C>
    implements Set<F>
{
    public ConvertingSet(C backend, Converter<B, F> converter) {
        super(backend, converter);
    }
}
