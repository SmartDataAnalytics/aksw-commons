package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Objects;
import java.util.Set;

public interface ObservableSet<T>
    extends ObservableCollection<T>, Set<T>
{
    /**
     * Return a field view for whether the given item is present.
     * Changing the value to true adds the given item to the set; on false
     * the item is removed.
     */
    default ObservableValue<Boolean> fieldForPresence(T item) {
        return new ObservableValue<Boolean>() {
            // protected K k = key;

            @Override
            public Boolean get() {
                return ObservableSet.this.contains(item);
            }

            @Override
            public void set(Boolean onOrOff) {
                if (!Boolean.TRUE.equals(onOrOff)) {
                    ObservableSet.this.remove(item);
                } else {
                    ObservableSet.this.add(item);
                }
            }

            @Override
            public Registration addPropertyChangeListener(PropertyChangeListener listener) {
                return ObservableSet.this.addPropertyChangeListener(ev -> {
                    Boolean oldValue = ((Set<T>)ev.getOldValue()).contains(item);
                    Boolean newValue = ((Set<T>)ev.getNewValue()).contains(item);

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
