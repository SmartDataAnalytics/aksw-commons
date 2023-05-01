package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.io.input.HasPosition;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.hadoop.fs.Seekable;



/** Combines Hadoop's Seekable and InputStream into one class */
public class SeekableInputStream
    extends ProxyInputStream implements SeekableDecorator, HasPosition
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
}

