package org.aksw.commons.rx.cache.range;

public class RangeBufferDelegateMutableImpl<A>
    extends RangeBufferDelegateBase<A>
    implements RangeBufferDelegateMutable<A>
{
    protected RangeBuffer<A> delegate;

    public RangeBufferDelegateMutableImpl() {
        this(null);
    }

    public RangeBufferDelegateMutableImpl(RangeBuffer<A> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public RangeBuffer<A> getDelegate() {
        return delegate;
    }

    @Override
    public void setDelegate(RangeBuffer<A> delegate) {
        this.delegate = delegate;
    }

}
