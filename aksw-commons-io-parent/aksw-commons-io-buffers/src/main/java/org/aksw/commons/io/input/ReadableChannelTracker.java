package org.aksw.commons.io.input;

import java.io.IOException;

public class ReadableChannelTracker<A, X extends ReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
{
    protected long totalReadBytes;
    protected long totalReadDuration;

    public ReadableChannelTracker(X delegate) {
        super(delegate);
    }

    // TODO Track number of bytes read + time spent
    // On close add the amount to the source - so stats are best accessed on the source.
//    @Override
//    public int read(A array, int position, int length) throws IOException {
//    }
}
