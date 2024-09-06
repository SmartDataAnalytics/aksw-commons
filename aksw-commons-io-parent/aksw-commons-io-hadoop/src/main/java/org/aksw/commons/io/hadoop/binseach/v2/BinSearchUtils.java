package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.io.binseach.BinSearchScanState;
import org.aksw.commons.io.hadoop.SeekableInputStream;
import org.aksw.commons.io.hadoop.SeekableInputStreams;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;

public class BinSearchUtils {
    /** Find the delimiter byte within the next 'allowedSearchBytes' bytes. Returns -1 if none encountered. Returns number of bytes read. */
    public static long readUntilDelimiter(InputStream in, byte delimiter, long allowedSearchBytes) throws IOException {
        long result = 0;
        while (true) {
            if (result >= allowedSearchBytes) {
                result = -1;
                break;
            }
            int c = in.read();
            if (c == -1) {
                result = -1;
                break;
            }

            ++result;
            if (c == delimiter) {
                break;
            }
        }
        return result;
    }


    public static int compareToPrefix(InputStream in, byte[] prefix) throws IOException {
        int cmp = 0;
        // Compare the next bytes with the prefix
        for (int i = 0; i < prefix.length; ++i) {
            int b = in.read();
            if (b == -1) {
                // eof -> search left
                cmp = -1;
                break;
            }

            int x = prefix[i];
            cmp = x - b;

            if (cmp != 0) {
                break;
            }
        }
        return cmp;
    }

    public static InputStream configureStream(
            SeekableReadableChannel<byte[]> channel, long end, byte[] prefix, BinSearchLevelCache levelCache) throws IOException {
        InputStream result;
        SeekableInputStream in = SeekableInputStreams.create(channel);
        Match match = BinarySearcherOverPlainSource.binarySearch(in,SearchMode.BOTH, 0, 0, end, (byte)'\n', prefix, levelCache);
        if (match != null) {
            in.position(match.start());

            BinSearchScanState scanState = new BinSearchScanState();
            scanState.firstDelimPos = match.start();
            scanState.matchDelimPos = match.end();
            scanState.prefixBytes = prefix;
            scanState.size = Long.MAX_VALUE;

            result =
                    ReadableChannels.newInputStream(
                    new ReadableByteChannelForLinesMatchingPrefix(
                            SeekableInputStreams.wrap(in), scanState));
        } else {
            System.err.println("NO MATCH for " + new String(prefix));
            in.close();
            result = InputStream.nullInputStream(); // ReadableChannels.newInputStream(ReadableChannels.limit(in, 0));
        }
        return result;
    }
}
