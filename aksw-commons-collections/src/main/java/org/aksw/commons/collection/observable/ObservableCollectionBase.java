package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.ConvertingCollection;

import com.google.common.base.Converter;

public class ObservableCollectionBase<T, C extends Collection<T>>
    extends AbstractCollection<T>
    implements ObservableCollection<T>
{
    protected C backend;

    protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ObservableCollectionBase(C backend) {
        this.backend = backend;
    }

    public Collection<T> getBackend() {
        return backend;
    }

    protected boolean isDuplicateAwareBackend() {
        return !(backend instanceof Set);
    }

    /**
     *
     * Removals are carried out as given in order for linked collections to yield items in the right order.
     * Hence, even if an item to be removed is contained in the additions it will be removed first.
     *
     */
    @Override
    public boolean delta(Collection<? extends T> rawAdditions, Collection<?> rawRemovals) {
        return ObservableCollectionOps.delta(
                this, backend,
                vcs, pcs,
                isDuplicateAwareBackend(),
                rawAdditions, rawRemovals);

//        boolean duplicateAware = isDuplicateAwareBackend();
//
//        @SuppressWarnings("unchecked")
//        Collection<T> removals = rawRemovals == this
//            ? StreamOps.collect(duplicateAware, rawRemovals.stream().map(x -> (T)x))
//            : StreamOps.collect(duplicateAware, rawRemovals.stream().filter(backend::contains).map(x -> (T)x));
//        Collection<T> additions = ObservableCollectionOps.collect(duplicateAware, rawAdditions.stream().filter(x -> !backend.contains(x) || removals.contains(x)));
//
//        boolean result = false;
//
//        {
//            Collection<T> oldValue = this;
//            Collection<T> newValue = rawRemovals == this
//                    ? additions
//                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, removals), additions);
//
//            try {
//                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                        this, oldValue, newValue,
//                        additions, removals, Collections.emptySet()));
//            } catch (PropertyVetoException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        boolean changeByRemoval;
//        if (rawRemovals == this) {
//            changeByRemoval = !backend.isEmpty();
//            if (changeByRemoval) {
//                // Only invoke clear if we have to; prevent triggering anything
//                backend.clear();
//            }
//        } else {
//            changeByRemoval = backend.removeAll(removals);
//        }
//
//        boolean changeByAddition = backend.addAll(additions);
//        result = changeByRemoval || changeByAddition;
//
//        {
//            Collection<T> oldValue = rawRemovals == this
//                    ? removals
//                    : CollectionOps.smartUnion(CollectionOps.smartDifference(backend, additions), removals);
//            Collection<T> newValue = this;
//
//            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                    this, oldValue, newValue,
//                    additions, removals, Collections.emptySet()));
//        }
//
//        return result;
    }


//    @Override
//    public boolean replace(Collection<? extends T> newValues) {
//
//        boolean result = !this.equals(newValues);
//
//        Collection<T> oldValueCopy = new LinkedHashSet<>(this);
//
//        if (result) {
//            {
//                Collection<T> oldValue = this;
//                Collection<T> newValue = new LinkedHashSet<>(newValues);
//
//                try {
//                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                            this, oldValue, newValue,
//                            CollectionOps.smartDifference(newValue, oldValue),
//                            CollectionOps.smartDifference(oldValue, newValue),
//                            Collections.emptySet()));
//                } catch (PropertyVetoException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            backend.clear();
//            backend.addAll(newValues);
//
//            {
//                // Set<T> newItem = Collections.singleton(value);
//                Collection<T> oldValue = oldValueCopy;
//                Collection<T> newValue = this;
//
//                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                        this, oldValue, newValue,
//                        CollectionOps.smartDifference(newValue, oldValue),
//                        CollectionOps.smartDifference(oldValue, newValue),
//                        Collections.emptySet()));
//            }
//
//        }
//
//        return result;
//    }

    @Override
    public boolean add(T value) {
        return addAll(Collections.singleton(value));
    }

    @Override
    public boolean addAll(Collection<? extends T> addedItems) {
        return delta(addedItems, Collections.emptySet());

//        boolean result = false;
//
//        Set<T> newItems = null;
//
//        if (isDuplicateAwareBackend() || !backend.containsAll(addedItems)) {
//            {
//                newItems = new LinkedHashSet<>(addedItems);
//                newItems.removeAll(backend);
//                // Set<T> newItem = Collections.singleton(value);
//
//                Collection<T> oldValue = this;
//                Collection<T> newValue = CollectionOps.smartUnion(backend, newItems);
//
//                try {
//                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                            this, oldValue, newValue,
//                            newItems, Collections.emptySet(), Collections.emptySet()));
//                } catch (PropertyVetoException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            result = backend.addAll(newItems);
//
//            {
//                // Set<T> newItem = Collections.singleton(value);
//                Collection<T> oldValue = CollectionOps.smartDifference(backend, newItems);
//                Collection<T> newValue = this;
//
//                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                        this, oldValue, newValue,
//                        newItems, Collections.emptySet(), Collections.emptySet()));
//            }
//
//        }
//
//        return result;
    }

    @Override
    public void clear() {
        removeAll(this);

//        if (!isEmpty()) {
//            {
//                Collection<T> oldValue = this;
//                Collection<T> removedItems = this;
//                Collection<T> newValue = Collections.emptySet();
//
//                try {
//                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                            this, oldValue, newValue,
//                            Collections.emptySet(), removedItems, Collections.emptySet()));
//                } catch (PropertyVetoException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            backend.clear();
//
//            {
//                Collection<T> oldValue = CollectionOps.smartUnion(backend, removedItems);
//                Collection<T> newValue = backend;
//
//                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                        this, oldValue, newValue,
//                        Collections.emptySet(), removedItems, Collections.emptySet()));
//            }
//        }
    }

    @Override
    public boolean remove(Object o) {
        return removeAll(Collections.singleton(o));
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        return delta(Collections.emptySet(), c);

//        Set<T> removedItems = c.stream().filter(backend::contains).map(x -> (T)x)
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//
//        boolean result = false;
//        if (isDuplicateAwareBackend() || !removedItems.isEmpty()) {
//            {
//                Collection<T> oldValue = backend;
//                Collection<T> newValue = CollectionOps.smartDifference(backend, removedItems);
//
//                try {
//                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
//                            this, oldValue, newValue,
//                            Collections.emptySet(), removedItems, Collections.emptySet()));
//                } catch (PropertyVetoException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            result = backend.removeAll(removedItems);
//
//            {
//                Collection<T> oldValue = CollectionOps.smartUnion(backend, removedItems);
//                Collection<T> newValue = backend;
//
//                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
//                        this, oldValue, newValue,
//                        Collections.emptySet(), removedItems, Collections.emptySet()));
//            }
//
//
//        }
//
//        return result;
    }

    protected static <F, B> Collection<F> convert(Collection<B> set, Converter<B, F> converter) {
        return set == null ? null : ConvertingCollection.createSafe(set, converter);
    }

    @Override
    public Iterator<T> iterator() {
        return backend.iterator();
    }

    @Override
    public int size() {
        return backend.size();
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
        return () -> vcs.removeVetoableChangeListener(listener);
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
    }

}
