package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.primitives.Ints;

/**
 * Wrap a stream such that reading is cut off at the first delimiter (including it) after a certain position.
 * Typically used to limit the stream to the first newline after a split.
 *
 * If the underlying channel's read method returns -2 it is interpreted as an end-of-block (EOB) marker.
 * This class limits the stream on the first delimiter after EOB.
 * This class hides any encountered block markers: Its read method either returns a positive non-zero integer for the number of bytes read
 * or -1 if either the final delimiter has been reached or the underlying channel has been consumed.
 *
 * @param <A>
 * @param <X>
 */
public class ReadableChannelWithLimitByDelimiter<A, X extends ReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
{
    // protected ThrowingPredicate<? super ReadableByteChannelWithConditionalBound<T>> testForEof;
    protected long nextSplitOffset; // the offset of the next split - it is allowed to read up to the next newline
    protected byte delimiter;
    protected boolean isInEofState = false;
    protected long bytesRead = 0;
    protected A excessBuffer;

    protected GetPosition getPosition;
    protected boolean isBlockMode;

    /**
     *
     * @param delegate
     * @param getPosition
     * @param isBlockMode If true then expect block boundaries to be advertised with -2 results of reads.
     * @param delimiter
     * @param nextSplitOffset
     */
    public ReadableChannelWithLimitByDelimiter(X delegate, GetPosition getPosition, boolean isBlockMode, byte delimiter, long nextSplitOffset) {
        super(delegate);
        this.getPosition = getPosition;
        this.isBlockMode = isBlockMode;
        this.delimiter = delimiter;
        this.nextSplitOffset = nextSplitOffset;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int result = 0;
        while (result == 0) {
            if (isInEofState) {
                result = -1;
            } else {
                long pos = getPosition.call();
                boolean isInNextSplit = pos >= nextSplitOffset;
                if (!isInNextSplit) {
                    int allowed = isBlockMode
                            ? length
                            : Math.min(length, Ints.saturatedCast(nextSplitOffset - pos));
                    result = getDecoratee().read(array, position, allowed);
                } else {
                    // Read into a tmp buffer and stop on the first delimiter
                    int excessBufferSize = 1024;
                    ArrayOps<A> arrayOps = getArrayOps();
                    if (excessBuffer == null) {
                        excessBuffer = getArrayOps().create(excessBufferSize);
                    }

                    int l = Math.min(length, excessBufferSize);

                    int n = getDecoratee().read(excessBuffer, 0, l);
                    if (n >= 0) {
                        int i;
                        for (i = 0; i < n; ++i) {
                            byte b = arrayOps.getByte(excessBuffer, i);
                            if (b == delimiter) {
                                // include the delimiter in the output:
                                // if "get(0) == delim" then copy 1 byte
                                ++i;
                                isInEofState = true;
                                // XXX Add support for consuming subsequent \r
                                break;
                            }
                        }
                        arrayOps.copy(excessBuffer, 0, array, position, i);
                        result = i;
                    } else {
                        result = n;
                    }
                }
            }

            if (result > 0) {
                bytesRead += result;
            } else if (result == -1) {
                isInEofState = true;
            } else if (result == -2) {
                if (!isBlockMode) {
                    throw new IllegalStateException("read return -2 but block mode was not enabled on this channel.");
                }
                // block end reached, re-read
                result = 0;
                continue;
            } else if (result == 0) {
                throw new RuntimeException("Zero-byte read.");
            } else {
                throw new RuntimeException("Unknown negative value: " + result);
            }

            break;
        }

        return result;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
