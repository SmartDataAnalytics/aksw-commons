package org.aksw.commons.io.input;

import java.io.IOException;

public class SeekableReadableChannelOmitBlockMarker<A, X extends SeekableReadableChannel<A>>
    extends SeekableReadableChannelDecoratorBase<A, X>
{
    public SeekableReadableChannelOmitBlockMarker(X delegate) {
        super(delegate);
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int result;
        while (true) {
            result = super.read(array, position, length);
            if (result != -2) {
                break;
            }
        }
        return result;
    }
}
