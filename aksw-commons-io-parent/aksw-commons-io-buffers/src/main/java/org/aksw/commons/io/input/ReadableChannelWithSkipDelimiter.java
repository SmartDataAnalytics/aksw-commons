package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.ring.RingBufferForBytes;

public class ReadableChannelWithSkipDelimiter<T extends ReadableChannel<byte[]>>
    extends ReadableChannelDecoratorBase<byte[], T>
{
    protected final byte delimiter;

    protected RingBufferForBytes ringBuffer;
    protected int initialSkipCount;
    protected int remainingSkipCount;

    public ReadableChannelWithSkipDelimiter(T delegate, byte delimiter, int initialSkipCount) {
        super(delegate);
        this.delimiter = delimiter;
        this.initialSkipCount = initialSkipCount;
        this.remainingSkipCount = initialSkipCount;
        this.ringBuffer = new RingBufferForBytes(8 * 1024);
    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {
        int result = 0;
        while (remainingSkipCount > 0) {
            if (ringBuffer.isEmpty()) {
                int n = ringBuffer.fill(getDecoratee(), ringBuffer.length());
                if (n == 0) {
                    result = -1;
                    break;
                }
            }
            int i;
            int availableData = ringBuffer.available();
            for (i = 0; i < availableData; ++i) {
                byte c = ringBuffer.get(i);
                if (c == delimiter) {
                    ++i; // Consume the delimiter
                    --remainingSkipCount;
                    break;
                }
            }
            ringBuffer.skip(i);
        }

        // Serve any remaining data from the ring buffer; afterwards delegate directly.
        if (!ringBuffer.isEmpty()) {
            result = ringBuffer.read(array, position, length);
        } else {
            result = getDecoratee().read(array, position, length);
        }

        return result;
    }
}
