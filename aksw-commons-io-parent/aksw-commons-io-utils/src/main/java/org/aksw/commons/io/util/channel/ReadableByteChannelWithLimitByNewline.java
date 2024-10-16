package org.aksw.commons.io.util.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ReadableByteChannelWithLimitByNewline<T extends SeekableByteChannel>
    extends ReadableByteChannelDecoratorBase<T>
{
    // protected ThrowingPredicate<? super ReadableByteChannelWithConditionalBound<T>> testForEof;
    protected long nextSplitOffset; // the offset of the next split - it is allowed to read up to the next newline
    protected boolean isInEofState = false;
    protected long bytesRead = 0;

    protected ByteBuffer excessBuffer;

    public ReadableByteChannelWithLimitByNewline(
            T delegate,
            long nextSplitOffset) {
        super(delegate);
        this.nextSplitOffset = nextSplitOffset;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        int result = 0;
        while (result == 0) {
            if (isInEofState) {
                result = -1;
            } else {
                long pos = getDelegate().position();
                boolean isInNextSplit = pos >= nextSplitOffset;
                if (!isInNextSplit) {
                    result = getDelegate().read(byteBuffer);
                } else {
                    // Read into a tmp buffer and check for newlines
                    if (excessBuffer == null) {
                        excessBuffer = ByteBuffer.allocate(1024 * 4);
                    } else {
                        excessBuffer.clear();
                    }
                    int n = getDelegate().read(excessBuffer);
                    if (n >= 0) {
                        int i;
                        for (i = 0; i < n; ++i) {
                            byte b = excessBuffer.get(i);
                            if (b == '\n') {
                                ++i;
                                isInEofState = true;
                                // XXX Add support for consuming subsequent \r
                                break;
                            }
                        }
                        excessBuffer.position(0);
                        excessBuffer.limit(i);
                        byteBuffer.duplicate().put(excessBuffer);
                        result = i;
                    } else {
                        result = n;
                    }
                }
            }

            if (result > 0) {
                bytesRead += result;
                break;
            } else if (result == -1) {
                isInEofState = true;
                break;
            } else if (result == -2) {
                // block end reached, re-read
                result = 0;
                continue;
            } else if (result == 0) {
                throw new RuntimeException("Zero-byte read.");
            } else {
                break;
            }
        }

        return result;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
