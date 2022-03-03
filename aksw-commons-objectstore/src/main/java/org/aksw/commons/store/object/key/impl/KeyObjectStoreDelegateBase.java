package org.aksw.commons.store.object.key.impl;

import org.aksw.commons.store.object.key.api.KeyObjectStore;

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
