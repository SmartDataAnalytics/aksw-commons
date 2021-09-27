package org.aksw.commons.index.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ForwardingMap;

/**
 * A completable map. Extends Map with a a flag whether the key set is 'complete'
 * w.r.t. some use case context such as caching.
 *
 */
public class CMapImpl<K, V, X>
    extends ForwardingMap<K, V>
    implements CMap<K, V, X>
{
    protected Map<K, V> delegate;
    protected X data;

    public CMapImpl() {
        this(new HashMap<K, V>());
    }

    public CMapImpl(Map<K, V> delegate) {
        this(delegate, null);
    }

    public CMapImpl(Map<K, V> delegate, X data) {
        super();
        this.delegate = delegate;
        this.data = data;
    }

    @Override
    protected Map<K, V> delegate() {
        return delegate;
    }

    @Override
    public X getData() {
        return data;
    }

    @Override
    public CMap<K, V, X> setData(X data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "CMap[" + delegate + ", " + data + "]";
    }
}
