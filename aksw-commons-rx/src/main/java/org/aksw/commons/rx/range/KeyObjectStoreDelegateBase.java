package org.aksw.commons.rx.range;

public class KeyObjectStoreDelegateBase
    implements KeyObjectStoreDelegate
{
    protected KeyObjectStore delegate;

    public KeyObjectStoreDelegateBase(KeyObjectStore delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public KeyObjectStore getDelegate() {
        return delegate;
    }
}
