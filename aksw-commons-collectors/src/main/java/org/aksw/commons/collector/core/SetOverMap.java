package org.aksw.commons.collector.core;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * A special helper class to treat a map as a collection of entries
 * and retain the ability to retrieve the map.
 *
 * Map.entrySet().add() is not always supported and its usually not
 * possible to get the map out again.
 */
public class SetOverMap<K, V>
    extends AbstractSet<Entry<K, V>>
{
    protected Map<K, V> delegate;

    /** For (de)serialization */
    SetOverMap() {
        super();
    }

    public SetOverMap(Map<K, V> delegate) {
        super();
        this.delegate = delegate;
    }

    public Map<K, V> getMap() {
        return delegate;
    }

    @Override
    public boolean add(Entry<K, V> e) {
        Objects.requireNonNull(e, "Entry must not be null");
        K key = e.getKey();

        V now = e.getValue();
        V old = delegate.put(key, now);
        boolean result = !Objects.equals(now, old);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = false;
        if (o instanceof Entry) {
            Object k = (Entry<?, ?>)o;
            result = delegate.containsKey(k);
            if (result) {
                delegate.remove(k);
            }
        }
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if (o instanceof Entry) {
            Entry<?, ?> e = (Entry<?, ?>)o;
            Object k = e.getKey();
            if (delegate.containsKey(k)) {
                V v = delegate.get(k);
                result = Objects.equals(v, e.getValue());
            }
        }
        return result;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return delegate.entrySet().iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SetOverMap<?, ?> other = (SetOverMap<?, ?>) obj;
        if (delegate == null) {
            if (other.delegate != null)
                return false;
        } else if (!delegate.equals(other.delegate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SetOverMap [delegate=" + delegate + "]";
    }
}
