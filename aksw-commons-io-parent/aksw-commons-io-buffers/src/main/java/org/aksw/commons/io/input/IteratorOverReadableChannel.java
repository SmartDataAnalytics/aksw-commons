package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.collections.CloseableIterator;
import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

public class IteratorOverReadableChannel<T>
    extends AbstractIterator<T>
	implements CloseableIterator<T>
{
    protected ReadableChannel<T[]> dataStream;

    protected ArrayOps<T[]> arrayOps;

    // We need to use Object because assigning arrays of primitive typesto T[]
    // raises a class cast exception
    protected Object array;
    protected int arrayLength;

    protected int currentOffset;
    protected int currentDataLength;


    /**
     *
     * @param arrayOps
     * @param dataStream
     * @param internalBufferSize The number of items to read from the dataStream at once.
     */
    public IteratorOverReadableChannel(ArrayOps<T[]> arrayOps, ReadableChannel<T[]> dataStream, int internalBufferSize) {
        super();
        Preconditions.checkArgument(internalBufferSize >= 0, "Internal buffer size must be greater than 0");

        this.arrayOps = arrayOps;
        this.dataStream = dataStream;
        this.arrayLength = internalBufferSize;
        this.array = arrayOps.create(internalBufferSize);

        this.currentDataLength = 0;

        // Initialized at end of buffer in order to trigger immediate read on next computeNext() call.
        this.currentOffset = 0;
    }

    @Override
    protected T computeNext() {
        if (currentOffset >= currentDataLength) {
            try {
                currentDataLength = dataStream.readRaw(array, 0, arrayLength);
                currentOffset = 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Object tmp;
        if (currentDataLength == -1) {
            tmp = endOfData();
        } else {
            tmp = arrayOps.getRaw(array, currentOffset);
            if (tmp == null) {
                throw new NullPointerException("Unexpected null value");
            }
        }

        ++currentOffset;

        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }

    @Override
    public void close() {
    	try {
			dataStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}
