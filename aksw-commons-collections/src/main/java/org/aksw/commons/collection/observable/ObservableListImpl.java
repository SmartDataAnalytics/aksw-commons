package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

// IMPLEMENTATION NOT FINISHED - its seems that an observable list is best modeled as a set of (index, value) entries
// in order to represent changes
// so maybe a map should be used for the backend
public class ObservableListImpl<T>
    extends ObservableCollectionBase<T, List<T>>
    implements ObservableList<T>
{

    public ObservableListImpl(List<T> backend) {
        super(backend);
    }

    public boolean deltaRaw(Map<Integer, ? extends T> additions, Set<Integer> removals) {
        return false;
    }


    @Override
    public boolean delta(Collection<? extends T> additions, Collection<?> removals) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T value) {
        deltaRaw(Collections.singletonMap(index, value), Collections.emptySet());
    }

    @Override
    public boolean addAll(int offset, Collection<? extends T> items) {
        Map<Integer, T> map = new HashMap<>();
        int i = 0;
        for (T item : items) {
            map.put(offset + (i++), item);
        }

        return deltaRaw(map, Collections.emptySet());
    }

    @Override
    public T get(int index) {
        return getBackend().get(index);
    }

    @Override
    public int indexOf(Object item) {
        return getBackend().indexOf(item);
    }

    @Override
    public int lastIndexOf(Object item) {
        return getBackend().lastIndexOf(item);
    }

    @Override
    public ListIterator<T> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T remove(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T set(int arg0, T arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> subList(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

}
