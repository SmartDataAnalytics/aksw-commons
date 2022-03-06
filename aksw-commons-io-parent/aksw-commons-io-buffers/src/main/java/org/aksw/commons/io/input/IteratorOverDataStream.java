package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.collect.AbstractIterator;

public class IteratorOverDataStream<T>
    extends AbstractIterator<T>
{
    protected DataStream<T[]> dataStream;

    protected ArrayOps<T[]> arrayOps;
    protected T[] array;
    protected int arrayLength;

    protected int currentOffset;
    protected int currentDataLength;


    public IteratorOverDataStream(ArrayOps<T[]> arrayOps, DataStream<T[]> reader) {
        super();
        this.arrayOps = arrayOps;
        this.dataStream = reader;
        this.arrayLength = 4096;
        this.array = arrayOps.create(arrayLength);

        this.currentDataLength = 0;

        // Initialized at end of buffer in order to trigger immediate read on next computeNext() call.
        this.currentOffset = 0;
    }

    @Override
    protected T computeNext() {
        if (currentOffset >= currentDataLength) {
            try {
                currentDataLength = dataStream.read(array, 0, arrayLength);
                currentOffset = 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Object tmp;
        if (currentDataLength == -1) {
            tmp = endOfData();
        } else {
            tmp = arrayOps.get(array, currentOffset);
            if (tmp == null) {
                throw new NullPointerException("Unexpected null value");
            }
        }

        ++currentOffset;

        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }
}
