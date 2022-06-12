package org.aksw.commons.io.buffer.plain;

import java.io.IOException;
import java.util.AbstractList;

import com.google.common.primitives.Ints;

/**
 * List view over a buffer.
 *
 * @param <T> The item type. Casts to this type are be unchecked.
 */
public class ListOverBuffer<T>
    extends AbstractList<T>
{
    protected Buffer<?> buffer;

    public ListOverBuffer(Buffer<?> buffer) {
        super();
        this.buffer = buffer;
    }

    @Override
    public T get(int index) {
        Object tmp;
        try {
            tmp = buffer.get(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }

    @Override
    public int size() {
        long capacity = buffer.getCapacity();
        return Ints.saturatedCast(capacity);
    }
}
