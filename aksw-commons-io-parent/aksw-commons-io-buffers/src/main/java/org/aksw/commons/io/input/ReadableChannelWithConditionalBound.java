package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Readable channel wrapper that before every read checks for an
 * end-of-file (eof) condition.
 * Once true, any subsequent read immediately returns -1 (eof).
 * Used to prevent reading across hadoop split boundaries
 */
public class ReadableChannelWithConditionalBound<A, X extends ReadableChannel<A>>
    extends ReadableChannelWithCounter<A, X>
{
    protected Predicate<? super ReadableChannelWithConditionalBound<A, X>> testForEof;
    protected boolean isInEofState = false;

    public ReadableChannelWithConditionalBound(
            X delegate,
            Predicate<? super ReadableChannelWithConditionalBound<A, X>> testForEof) {
        super(delegate);
        this.testForEof = testForEof;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        isInEofState = isInEofState || testForEof.test(this);

        int result;
        if (isInEofState) {
            result = -1;
        } else {
            result = super.read(array, position, length);
            if (result < 0) {
                isInEofState = true;
            }
        }

        return result;
    }
}
