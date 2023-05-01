package org.aksw.commons.util.collection;

import java.util.stream.Stream;

public interface CollectionOps<C, T, D extends C> {
    Class<?> getCollectionClass();

    long size(C col);
    Stream<T> items(C col);
    D newCollection(long size);
    void add(C dst, T item);

    /**
     * @return The number of removed items
     */
    long removeAll(C dst, T item);
}
