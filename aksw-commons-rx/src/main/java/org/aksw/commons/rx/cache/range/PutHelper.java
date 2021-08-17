package org.aksw.commons.rx.cache.range;

import java.lang.reflect.Array;

public interface PutHelper {
    void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength);

    default void put(int offset, Object item) {
        putAll(offset, new Object[]{ item });
    }

    default void putAll(int offset, Object arrayWithItemsOfTypeT) {
        putAll(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
    }
}
