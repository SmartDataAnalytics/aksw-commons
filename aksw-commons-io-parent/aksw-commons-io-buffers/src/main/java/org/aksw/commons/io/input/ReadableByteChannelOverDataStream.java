package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Wrapper to treat a DataStream<byte[]> as a {@link ReadableByteChannel}.
 * Use with {@link java.nio.channels.Channels#newInputStream(ReadableByteChannel)} to obtain
 * a conventional {@link java.io.InputStream} over a a {@link DataStream}.
 *
 * @author raven
 *
 */
public class ReadableByteChannelOverDataStream
    implements ReadableByteChannel
{
    protected DataStream<byte[]> dataStream;

    public ReadableByteChannelOverDataStream(DataStream<byte[]> dataStream) {
        super();
        this.dataStream = dataStream;
    }

    @Override
    public void close() throws IOException {
        dataStream.close();
    }

    @Override
    public boolean isOpen() {
        return dataStream.isOpen();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int r;
        int n = dst.remaining();
        if (dst.hasArray()) {
            r = dataStream.read(dst.array(), dst.arrayOffset() + dst.position(), n);
            if (r >= 0) {
                dst.position(dst.position() + r);
            }
        } else {
            byte[] buf = new byte[n];
            r = dataStream.read(buf, 0, n);

            if (r >= 0) {
                dst.put(buf, 0, r);
            }
        }

        return r;
    }

}
