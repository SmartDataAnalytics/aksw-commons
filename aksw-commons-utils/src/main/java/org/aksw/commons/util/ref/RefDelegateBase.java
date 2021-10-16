package org.aksw.commons.util.ref;

public class RefDelegateBase<T, R extends Ref<T>>
    implements RefDelegate<T, R>
{
    protected R delegate;

    public RefDelegateBase(R delegate) {
        super();
        this.delegate = delegate;
    }

    public R getDelegate() {
        return delegate;
    }
}