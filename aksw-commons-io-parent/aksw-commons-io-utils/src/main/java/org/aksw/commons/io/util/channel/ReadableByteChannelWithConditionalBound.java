package org.aksw.commons.io.util.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.lambda.throwing.ThrowingPredicate;

/**
 * Readable byte channel wrapper that before every read checks for an
 * end-of-file (eof) condition.
 * Once true, any subsequent read immediatly returns -1 (eof).
 * Used to prevent reading across hadoop split boundaries
 */
public class ReadableByteChannelWithConditionalBound<T extends ReadableByteChannel>
    extends ReadableByteChannelDecoratorBase<T>
{
    protected ThrowingPredicate<? super ReadableByteChannelWithConditionalBound<T>> testForEof;
    protected boolean isInEofState = false;
    protected long bytesRead = 0;

    public ReadableByteChannelWithConditionalBound(
            T delegate,
            ThrowingPredicate<? super ReadableByteChannelWithConditionalBound<T>> testForEof) {
        super(delegate);
        this.testForEof = testForEof;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (!isInEofState) {
            try {
                isInEofState = testForEof.test(this);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        int result;
        if (isInEofState) {
            result = -1;
        } else {
            result = getDelegate().read(byteBuffer);
            if (result >= 0) {
                bytesRead += result;
            } else {
                isInEofState = true;
            }
        }

        return result;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
