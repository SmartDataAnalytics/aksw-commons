package org.aksw.commons.index.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ForwardingMap;

/**
 * A completable map. Extends Map with a a flag whether the key set is 'complete'
 * w.r.t. some use case context such as caching.
 *
 */
public class CMapImpl<K, V>
    extends ForwardingMap<K, V>
    implements CMap<K, V>
{
    protected Map<K, V> delegate;
    protected boolean isKeySetComplete;

    public CMapImpl() {
        this(new HashMap<K, V>());
    }

    public CMapImpl(Map<K, V> delegate) {
        this(delegate, false);
    }

    public CMapImpl(Map<K, V> delegate, boolean isKeySetComplete) {
        super();
        this.delegate = delegate;
        this.isKeySetComplete = isKeySetComplete;
    }

    @Override
    protected Map<K, V> delegate() {
        return delegate;
    }

    @Override
    public boolean isComplete() {
        return isKeySetComplete;
    }

    @Override
    public void setComplete(boolean status) {
        this.isKeySetComplete = status;
    }
}
