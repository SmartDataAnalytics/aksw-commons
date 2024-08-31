package org.aksw.commons.io.buffer.plain;

import java.io.IOException;
import java.util.AbstractList;

/**
 * List view over a buffer.
 *
 * @param <T> The item type. Casts to this type are unchecked.
 */
public class ListOverBuffer<T>
    extends AbstractList<T>
{
    protected Buffer<?> buffer;
    protected int size;

    public ListOverBuffer(Buffer<?> buffer) {
        this(buffer, 0);
    }

    public ListOverBuffer(Buffer<?> buffer, int size) {
        super();
        this.buffer = buffer;
        this.size = size;
    }

    @Override
    public boolean add(T e) {
        set(size, e);
        ++size;
        return true;
    }

// TODO shift left
//    @Override
//    public T remove(int index) {
//        Object value = buffer.getArrayOps().getDefaultValue();
//        if (index + 1 == size) {
//            --size;
//        }
//        set(index, value);
//    }

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
    public T set(int index, T element) {
        try {
            buffer.put(index, element);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return element;
    }

    @Override
    public int size() {
        return size;
    }
}
