package org.aksw.commons.io.input;

import java.io.IOException;

public class CharSequenceOverSeekableReadableChannelOfBytes
    implements CharSequence
{
    protected SeekableReadableChannel<byte[]> seekable;
    protected int length;

    protected byte[] buffer = new byte[1];

    public CharSequenceOverSeekableReadableChannelOfBytes(SeekableReadableChannel<byte[]> seekable) {
        this(seekable, Integer.MAX_VALUE);
    }

    public CharSequenceOverSeekableReadableChannelOfBytes(SeekableReadableChannel<byte[]> seekable, int length) {
        this.seekable = seekable;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        char result;
        try {
            if (index >= length) {
                result = (char)-1;
            } else {
                seekable.position(index);
                int n = seekable.read(buffer, 0, 1);
                // seekable.position(p);

                if (n > 0) {
                    result = (char)buffer[0];
                } else if (n < 0) {
                    result = (char)-1;
                } else {
                    throw new IllegalStateException("Read 0 bytes");
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // We cannot guarantee that newly openend channels well be closed, therefore unsupported
        throw new UnsupportedOperationException();
//        Seekable clone = seekable.clone();
//        return new CharSequenceFromSeekable(clone, start, end);
    }
}