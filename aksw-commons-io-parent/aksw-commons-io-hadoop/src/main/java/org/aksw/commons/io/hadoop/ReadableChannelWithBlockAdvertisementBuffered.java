package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.ReadableChannelBase;

public class ReadableChannelWithBlockAdvertisementBuffered
    extends ReadableChannelBase<byte[]>
{
    protected InputStream decodedIn;
    protected org.apache.hadoop.fs.Seekable seekable;

    protected long startPos;
    protected long currentPos;
    protected byte[] buffer;

    protected byte pendingByte;

    // 0: no pending byte, 1: return -2 on next read, 2: return pending byte
    protected int pendingByteState;

    protected int endOfBlockMarker;
    protected long readCount = 0;

    public ReadableChannelWithBlockAdvertisementBuffered(InputStream decodedIn) throws IOException {
        this(decodedIn, -2);
    }

    public ReadableChannelWithBlockAdvertisementBuffered(InputStream decodedIn, int endOfBlockMarker) throws IOException {
        super();
        this.decodedIn = decodedIn;
        this.buffer = new byte[8 * 1024];
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
        if (pendingByteState == 1) {
            pendingByteState = 2;
            result = -2;
        } else if (pendingByteState == 2) {
            array[position] = pendingByte;
            result = 1;
            pendingByteState = 0;
        } else {
            int l = Math.min(length, buffer.length);
            result = decodedIn.read(buffer, 0, l);
            if (result > 0) {
                long nextPos = seekable.getPos();
                long effectiveCurrentPos = getCurrentPos();
                // Hadoop codec contract says that a read stops after the first byte in a block
                //   and this is when the position changes
                if (effectiveCurrentPos != (nextPos - 1)) {
                    // Unread the last byte
                    --result;
                    pendingByte = buffer[result];
                    if (result == 0) {
                        result = -2;
                        pendingByteState = 2;
                    } else {
                        // Return current data and schedule -2 result on next read
                        pendingByteState = 1;
                    }
                    currentPos = nextPos;
                }
                if (result > 0) {
                    getArrayOps().copy(buffer, 0, array, position, result);
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
