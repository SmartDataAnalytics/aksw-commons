package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Iterator;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.collect.AbstractIterator;

public class SequentialReaderIterator<T>
    extends AbstractIterator<T>
{
    protected SequentialReader<T[]> reader;

    protected ArrayOps<T[]> arrayOps;
    protected T[] array;
    protected int arrayLength;

    protected int currentOffset;
    protected int currentDataLength;


    public SequentialReaderIterator(ArrayOps<T[]> arrayOps, SequentialReader<T[]> reader) {
        super();
        this.arrayOps = arrayOps;
        this.reader = reader;
        this.arrayLength = 4096;
        this.array = arrayOps.create(arrayLength);

        this.currentDataLength = 0;

        // Initialized at end of buffer in order to trigger immediate read on next computeNext() call.
        this.currentOffset = 0;
    }


    public static <T> Iterator<T> create(ArrayOps<T[]> arrayOps, SequentialReader<T[]> reader) {
        return new SequentialReaderIterator<>(arrayOps, reader);
    }


    @Override
    protected T computeNext() {
        if (currentOffset >= currentDataLength) {
            try {
                currentDataLength = reader.read(array, 0, arrayLength);
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
