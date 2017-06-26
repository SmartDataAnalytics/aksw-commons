package org.aksw.commons.collections.trees;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public class ReclaimingSupplier<T> implements Supplier<T> {
    protected Supplier<T> generator;
    protected Set<T> reclaims = new TreeSet<T>();

    public ReclaimingSupplier(Supplier<T> generator) {
        super();
        this.generator = generator;
    }

    public void reclaim(T item) {
        reclaims.add(item);
    }

    @Override
    public T get() {
        T result;
        Iterator<T> it = reclaims.iterator();
        if (it.hasNext()) {
            result = it.next();
            it.remove();
        } else {
            result = generator.get();
        }
        return result;
    }
}
