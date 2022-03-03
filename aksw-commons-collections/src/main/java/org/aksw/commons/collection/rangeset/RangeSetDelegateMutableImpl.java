package org.aksw.commons.collection.rangeset;

import com.google.common.collect.RangeSet;

public class RangeSetDelegateMutableImpl<T extends Comparable<T>>
    extends RangeSetDelegateBase<T>
    implements RangeSetDelegateMutable<T>
{
    protected RangeSet<T> delegate;

    @Override
    public RangeSet<T> getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(RangeSet<T> delegate) {
        this.delegate = delegate;
    }

}
