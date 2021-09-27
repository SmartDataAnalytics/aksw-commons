package org.aksw.commons.index.util;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;

public class CSetImpl<T, X>
    extends ForwardingSet<T>
    implements CSet<T, X>
{
    protected Set<T> delegate;
    protected X data;

    public CSetImpl() {
        this(new HashSet<T>());
    }

    public CSetImpl(Set<T> delegate) {
        this(delegate, null);
    }

    public CSetImpl(Set<T> delegate, X data) {
        super();
        this.delegate = delegate;
        this.data = data;
    }

    @Override
    protected Set<T> delegate() {
        return delegate;
    }

    @Override
    public X getData() {
        return data;
    }

    @Override
    public CSet<T, X> setData(X data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "CSet[" + delegate + ", " + data + "]";
    }
}
