package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.io.binseach.BinSearchScanState;
import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.ring.RingBufferForBytes;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannels;

import com.google.common.primitives.Ints;

public class ReadableByteChannelForLinesMatchingPrefix
    implements ReadableChannel<byte[]> {

    protected ReadableChannel<byte[]> channel;
    protected BinSearchScanState state;

    protected byte delimiter = (byte)'\n';

    protected long position;
    protected long knownDelimPos;

    // Initialized upon first read past knownDelimPos
    protected boolean isCurrentLineValidated = true;
    protected RingBufferForBytes buffer;

    protected boolean endReached = false;

    public ReadableByteChannelForLinesMatchingPrefix(ReadableChannel<byte[]> channel, BinSearchScanState state) {
        this.channel = channel;
        this.state = state;

        this.position = state.firstDelimPos;//Math.max(state.firstPos, 0);
        this.knownDelimPos = state.matchDelimPos;
    }

    @Override
    public int read(byte[] array, int offset, int length) throws IOException {
        int result;
        if (position < knownDelimPos) {
            // If we haven't read past the known delim pos then we don't need validation
            int remaining = Ints.saturatedCast(knownDelimPos - position);
            int l = Math.min(length, remaining);
            result = channel.read(array, offset, l);
        } else {
            if (endReached) {
                result = -1;
            } else {
                int l = state.prefixBytes.length;
                if (buffer == null) {
                    buffer = new RingBufferForBytes(Math.max(4 * 1024, l));//  new byte[4 * 1024];
                }

                if (!isCurrentLineValidated) {
                    // after knowDelimPos we need to verify each line

                    // The buffer is positioned at a delimiter so the next read will return the next 'line'
                    // Fill sufficient bytes so that we can compare the prefix
                    buffer.fill(channel, l);
                    if (buffer.available() < l) {
                        // Insufficient available data
                        result = -1;
                    } else {
                        try (InputStream in = ReadableChannels.newInputStream(buffer.shallowClone())) {
                            int cmp = BinSearchUtils.compareToPrefix(in, state.prefixBytes);
                            isCurrentLineValidated = cmp == 0;
                        }
                    }
                }
            }

            if (!isCurrentLineValidated) {
                endReached = true;
                result = -1;
            } else {
                // Serve up to the next newline or the remaining buffer
                int n = buffer.available();
                if (n == 0) {
                    n = buffer.fill(channel);
                }

                if (n == 0) {
                    result = -1;
                } else {
                    int i;
                    for (i = 0; i < n; ++i) {
                        int c = buffer.get(i);
                        if (c == delimiter) {
                            ++i; // include the delimiter in the read
                            isCurrentLineValidated = false;
                            break;
                        }
                    }

                    // Serve all data up to the delimiter
                    result = ReadableChannels.readFully(buffer, array, offset, i);
                }
            }
        }

        if (result > 0) {
            position += result;
        }

        return result;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }
}
