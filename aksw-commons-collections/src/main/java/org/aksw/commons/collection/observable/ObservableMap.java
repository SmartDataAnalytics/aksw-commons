package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Map;
import java.util.Objects;

/**
 * Interface based on {@link Map} with extensions for observing changes.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public interface ObservableMap<K, V>
    extends Map<K, V>
{
    @Override
    ObservableSet<K> keySet();

//    @Override
//    ObservableSet<Entry<K, V>> entrySet();
//
//    @Override
//    ObservableCollection<V> values();

    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Registration addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Observe a key's value
     *
     * @param key
     * @return
     */
    default ObservableValue<V> observeKey(K key) {
        return observeKey(key, null);
    }

    /**
     * Observe a key's value. Yield a default value if the key does not exist or its value is null.
     *
     * @param key
     * @return
     */
    default ObservableValue<V> observeKey(K key, V defaultValue) {
        return new ObservableValue<V>() {
            // protected K k = key;

            @Override
            public V get() {
                return ObservableMap.this.getOrDefault(key, defaultValue);
            }

            @Override
            public void set(V value) {
                if (value == null) {
                    ObservableMap.this.remove(key);
                } else {
                    ObservableMap.this.put(key, value);
                }
            }

            @Override
            public Registration addPropertyChangeListener(PropertyChangeListener listener) {
                return ObservableMap.this.addPropertyChangeListener(ev -> {
                    V oldValue = ((Map<K, V>)ev.getOldValue()).getOrDefault(key, defaultValue);
                    V newValue = ((Map<K, V>)ev.getNewValue()).getOrDefault(key, defaultValue);

                    if (oldValue != null && newValue != null && !Objects.equals(oldValue, newValue)) {
                        listener.propertyChange(new PropertyChangeEvent(this, "value", oldValue, newValue));
                    }

                });
            }

            @Override
            public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
                throw new UnsupportedOperationException();
            }
        };
    }
}

