package org.aksw.commons.index.util;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;

public class CSetImpl<T>
    extends ForwardingSet<T>
    implements CSet<T>
{
    protected Set<T> delegate;
    protected boolean isComplete;

    public CSetImpl() {
        this(new HashSet<T>());
    }

    public CSetImpl(Set<T> delegate) {
        this(delegate, false);
    }

    public CSetImpl(Set<T> delegate, boolean isKeySetComplete) {
        super();
        this.delegate = delegate;
        this.isComplete = isKeySetComplete;
    }

    @Override
    protected Set<T> delegate() {
        return delegate;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public void setComplete(boolean status) {
        this.isComplete = status;
    }

}
