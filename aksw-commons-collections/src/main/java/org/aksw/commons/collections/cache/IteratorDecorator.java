package org.aksw.commons.collections.cache;

import java.util.Iterator;

import com.google.common.collect.ForwardingIterator;

public class IteratorDecorator<T>
    extends ForwardingIterator<T>
{
    protected Iterator<T> delegate;
    protected long numItems;

    public IteratorDecorator(Iterator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Iterator<T> delegate() {
        return delegate;
    }
}
