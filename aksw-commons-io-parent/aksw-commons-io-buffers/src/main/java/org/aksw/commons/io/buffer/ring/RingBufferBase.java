package org.aksw.commons.io.buffer.ring;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.io.input.ReadableSource;

/**
 * A fixed size buffer with start and end pointers.
 * The read operations increment the start pointer.
 * The fill operation reads from a source and increments the end pointer.
 * Both pointers 'overflow' when reaching the end of the array and start from offset 0 again.
 *
 * If a read operation causes the start and end pointers to meet then the buffer is considered empty.
 * If a fill operation causes the start and end pointers to meet then the buffer is considered full.
 * A flag is used to discriminate those cases.
 *
 * @param <A>
 */
public abstract class RingBufferBase<A>
    implements ReadableSource<A>
{
    protected final A buffer;
    protected final int bufferLen;

    protected int start;
    protected int end;
    protected boolean isEmpty; // true when start meets end

    public RingBufferBase(A buffer) {
        this(buffer, 0, 0, true);
    }

    public RingBufferBase(A buffer, int start, int end, boolean isEmpty) {
        super();
        this.buffer = Objects.requireNonNull(buffer);
        this.bufferLen = getArrayOps().length(buffer);
        this.start = start;
        this.end = end;
        this.isEmpty = isEmpty;

        if (isEmpty && !(start == end)) {
            throw new IllegalArgumentException("When setting isEmpty=true then it must hold that start==end");
        }
    }

    /**
     *
     * @param source
     * @param targetAmount Fill the buffer to this amount (NOT the amount to read from the source)
     * @return
     * @throws IOException
     */
    public int fill(ReadableSource<A> source, int targetAmount) throws IOException {
        int result = 0;
        while (available() < targetAmount) {
            int contrib = fill(source);
            if (contrib == 0) {
                // Contribution of 0 bytes means EOF reached
                break;
            }
            result += contrib;
        }
        return result;
    }

    public int fill(ReadableSource<A> source) throws IOException {
        // If the end marker reaches bufferLen then it is immediately normalized to 0.
        // Se the end marker never equals bufferLen.

        int n;
        if (start < end || isEmpty) {
            // [end, bufferLen)
            int remainingSpace = bufferLen - end;
            n = source.read(buffer, end, remainingSpace);
            if (n > 0) {
                end += n;
            }
        } else {
            // [end, start)
            int d = start - end;
            if (d == 0) { // !isEmpty is implied here -  && !isEmpty) {
                n = 0; // no capacity
            } else {
                n = source.read(buffer, end, d);
                if (n > 0) {
                    end += n;
                }
            }
        }

        if (n > 0) {
            isEmpty = false;
            if (end == bufferLen) {
                end = 0;
            } else if (end > bufferLen) {
                throw new IllegalStateException("Should not happen: End pointer exceeded buffer length");
            }
        }

        if (n < 0) {
            n = 0;
        }

        return n;
    }

    public int available() {
        return bufferLen - capacity();
    }

    // The number of unused bytes - distance between end and start
    public int capacity() {
        return isEmpty
            ? bufferLen
            : end > start
                ? (bufferLen - end) + start
                : start - end;
    }

    public int length() {
        return bufferLen;
    }

    /** Increment the start pointer by the given amount. Raises an invalid argument exception if the length is
     *  greater than {@link #available()}. */
    public void skip(int length) {
        int n = available();
        if (length > n) {
            throw new IllegalArgumentException("Requested skipping " + length + " elements but only " + n + " available.");
        }

        start += length;
        if (start >= bufferLen) {
            start -= bufferLen;
        }

        if (start == end) {
            isEmpty = true;
        }
    }

    @Override
    public int read(A tgt, int tgtOffset, int length) throws IOException {
        int result;
        if (start == end && isEmpty) {
            result = -1;
        } else {
            if (start < end) {
                int remainingSpace = end - start;
                result = Math.min(remainingSpace, length);
                getArrayOps().copy(buffer, start, tgt, tgtOffset, result);
            } else {
                int remainingSpace = bufferLen - start;
                result = Math.min(remainingSpace, length);
                getArrayOps().copy(buffer, start, tgt, tgtOffset, result);
            }

            if (result > 0) {
                start += result;
                if (start == bufferLen) {
                    start = 0;
                }

                if (start == end) {
                    start = 0;
                    end = 0;
                    isEmpty = true;
                }
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
