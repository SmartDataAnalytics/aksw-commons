package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.io.input.HasPosition;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.compress.CompressionInputStream;

/**
 * Combines Hadoop's Seekable and InputStream into one class.
 *
 * @implNote
 *   This class inherits the {@link PositionedReadable} interface
 *   but all methods raise an {@link UnsupportedOperationException}.
 *   The reason is that {@link CompressionInputStream} requires it for position to work,
 *   due to an interface check:
 *   <pre>
 *     protected CompressionInputStream(InputStream in) throws IOException {
 *     if (!(in instanceof Seekable) || !(in instanceof PositionedReadable)) ...
 *   </pre>
 */
public class SeekableInputStream
    extends ProxyInputStream implements SeekableDecorator, HasPosition,
        PositionedReadable
{
    protected Seekable seekable;

    /**
     * Constructs a new ProxyInputStream.
     *
     * @param proxy the InputStream to delegate to
     */
    public SeekableInputStream(InputStream proxy, Seekable seekable) {
        super(proxy);
        this.seekable = seekable;
    }

    @Override
    public Seekable getSeekable() {
        return seekable;
    }

    @Override
    public long position() {
        try {
            return getSeekable().getPos();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void position(long pos) {
        try {
            getSeekable().seek(pos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        throw new UnsupportedOperationException();
    }
}

