package org.aksw.commons.io.hadoop;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.ReadableChannelBase;

/** This variant assumes that the underlying encoded stream runs in BLOCK_MODE.
 *  Reads are expected to:
 *  <ol>
 *    <li>stop after yielding the first byte of a block (that byte is returned) and<li>
 *    <li>these reads always return 1 (never anything greater)</li>
 *    <li>the position indicating the block id is updated after that read.</li>
 *  </ol>
 *
 *  {@link ReadableChannelWithBlockAdvertisementBuffered} also assumes that every read stops after
 *  yielding the first byte of a block, however the read length may be greater than 1.
 */
public class ReadableChannelWithBlockAdvertisement
    extends ReadableChannelBase<byte[]>
{
    protected InputStream decodedIn;
    protected org.apache.hadoop.fs.Seekable seekable;

    protected long startPos;
    protected long currentPos;

    protected byte pendingByte;
    protected boolean hasPendingByte = false;

    protected int endOfBlockMarker;
    protected long readCount = 0;

    public ReadableChannelWithBlockAdvertisement(InputStream decodedIn) throws IOException {
        this(decodedIn, -2);
    }

    public ReadableChannelWithBlockAdvertisement(InputStream decodedIn, int endOfBlockMarker) throws IOException {
        super();
        this.decodedIn = decodedIn;
        this.seekable = (org.apache.hadoop.fs.Seekable)decodedIn;
        this.endOfBlockMarker = endOfBlockMarker;
        this.startPos = seekable.getPos();
        this.currentPos = startPos;
    }

    public long position() throws IOException {
        return seekable.getPos();
    }

    public long getStartPos() {
        return startPos;
    }

    public long getCurrentPos() {
        // The bzip2 codec reports one byte ahead of the block boundary
        return currentPos == 0 ? 0 : currentPos - 1;
    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be non-zero and positive");
        }

        int result;
        if (hasPendingByte) {
            array[position] = pendingByte;
            result = 1;
            hasPendingByte = false;
        } else {
            byte backupByte = array[position];
            result = decodedIn.read(array, position, length);
            if (result == 1) {
                // Hadoop splittable codec contract says that a read stops after the first byte in a block
                //   and this is when the position changes
                long nextPos = seekable.getPos();
                long effectiveCurrentPos = getCurrentPos();
                if (effectiveCurrentPos != (nextPos - 1)) {
                    // Unread the last byte
                    pendingByte = array[position];
                    hasPendingByte = true;
                    array[position] = backupByte;
                    currentPos = nextPos;
                    result = -2;
                }
            }
        }

        if (result > 0) {
            readCount += result;
        }
        return result;
    }

    protected int onBlockEnd(ByteBuffer dst, long oldPos, long newPos) {
        return endOfBlockMarker;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    /** Discard bytes from this channel until a -2 read result is encountered. */
    public boolean adjustToNextBlock() throws IOException {
        byte[] buf = new byte[8 * 1024];
        int tmp;
        int bytesRead = 0;
        while ((tmp = read(buf, 0, buf.length)) >= 0) { bytesRead += tmp; }
        System.out.println("Bytes read: " + bytesRead);
        boolean result = tmp == -2;
        return result;
    }
}
