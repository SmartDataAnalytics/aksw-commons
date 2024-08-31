package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * Wrapper to treat a DataStream<byte[]> as a {@link ReadableByteChannel}.
 * Use with {@link java.nio.channels.Channels#newInputStream(ReadableByteChannel)} to obtain
 * a conventional {@link java.io.InputStream} over a a {@link ReadableChannel}.
 *
 * @author raven
 *
 */
public class ReadableByteChannelAdapter<T extends ReadableChannel<byte[]>>
    implements ReadableByteChannel
{
    protected T delegate;

    /** Buffer is only initialized when non-array-backed ByteBuffer are passed to {@link #read(ByteBuffer)}. */
    protected byte[] buf;
    protected final int transferSize = 8 * 1024;

    public ReadableByteChannelAdapter(T delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public T getDelegate() {
        return delegate;
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int r;
        int n = dst.remaining();
        if (dst.hasArray()) {
            byte[] arr = dst.array();
            int arrPos = dst.arrayOffset() + dst.position();
            r = getDelegate().read(arr, arrPos, n);
            if (r > 0) {
                dst.position(dst.position() + r);
            }
        } else {
            if (buf == null) {
                buf = new byte[transferSize];
            }
            int l = Math.min(buf.length, n);
            r = getDelegate().read(buf, 0, l);
            if (r > 0) {
                dst.put(buf, 0, r);
            }
        }
        return r;
    }
}
