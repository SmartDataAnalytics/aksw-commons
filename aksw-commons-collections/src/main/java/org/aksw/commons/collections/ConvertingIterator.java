package org.aksw.commons.collections;

import java.util.Iterator;

import com.google.common.base.Converter;

public class ConvertingIterator<T, U, I extends Iterator<U>>
    implements Iterator<T>
{
    protected I core;
    protected Converter<U, T> converter;

    public ConvertingIterator(I core, Converter<U, T> converter) {
        super();
        this.core = core;
        this.converter = converter;
    }

    @Override
    public T next() {
        U raw = core.next();
        T result = converter.convert(raw);
        return result;
    }

    @Override
    public boolean hasNext() {
        boolean result = core.hasNext();
        return result;
    }

    @Override
    public void remove() {
        core.remove();
    }
}