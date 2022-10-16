package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

import org.aksw.commons.io.hadoop.binseach.bz2.ReadableByteChannelBase;


public class ReadableChannelWithBlockAdvertisement
    extends ReadableByteChannelBase
{
    protected InputStream decodedIn;
    protected org.apache.hadoop.fs.Seekable seekable;
    protected long decodedStartPos;
    protected PushbackInputStream pushbackIn;
    protected long readCount = 0;

    public ReadableChannelWithBlockAdvertisement(InputStream decodedIn) {
        seekable = (org.apache.hadoop.fs.Seekable)decodedIn;
        try {
            decodedStartPos = seekable.getPos();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // We need to check one byte in advance to detect block boundaries
        pushbackIn = new PushbackInputStream(decodedIn, 1);
    }

    @Override
    protected int readActual(ByteBuffer dst) throws IOException {
        int backupPos = dst.position();
        byte before = dst.get(backupPos);

        int result = super.read(dst);

        if (result > 0) {
            readCount += result;
        }

        // If only a single byte was read and the position changed then
        // undo the read and indicate end-of-block (file)
        if (result == 1) {
            long decodedPos = seekable.getPos();
            boolean change = decodedStartPos != decodedPos;

            if (change) {
                // Unread the byte
                byte after = dst.get(backupPos);
                pushbackIn.unread(after);

                // Revert the buffer state
                dst.put(backupPos, before);
                dst.position(backupPos);


                result = onBlockEnd(dst, decodedStartPos, decodedPos);

                decodedStartPos = decodedPos;

                // result = onBlockEnd(dst, oldPos, newPos);// endOfBlockMarker;
            }
        }
        return result;
    }

    protected int onBlockEnd(ByteBuffer dst, long oldPos, long newPos) {
        return -2;
    }
}
