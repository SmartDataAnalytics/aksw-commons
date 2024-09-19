package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.hadoop.SeekableInputStream;
import org.aksw.commons.io.hadoop.binseach.v2.BinSearchResourceCache.CacheEntry;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannelSupplier;
import org.aksw.commons.io.input.ReadableChannelWithLimitByDelimiter;
import org.aksw.commons.io.input.ReadableChannelWithSkipDelimiter;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.aksw.commons.io.input.SeekableReadableSourceWithMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binary search over an non-encoded SeekableInputStreams.
 * Non-encoded means that every byte is individually addressable
 * (in contrast to block-based stream/channels).
 */
public class BinarySearcherOverPlainSource
    implements BinarySearcher
{
    private static final Logger logger = LoggerFactory.getLogger(BinarySearcherOverPlainSource.class);

    protected SeekableReadableChannelSource<byte[]> source;
    protected Supplier<CacheEntry> cacheSupplier;

    protected BinarySearcherOverPlainSource(SeekableReadableChannelSource<byte[]> source, Supplier<CacheEntry> cacheSupplier) {
        super();
        this.source = source;
        this.cacheSupplier = cacheSupplier;
    }

    @Override
    public void close() throws Exception {
    }

    public static Match binarySearch(SeekableInputStream channel, long end, byte[] prefix) throws IOException {
        return binarySearch(channel, SearchMode.BOTH, 0, 0, end, end, (byte)'\n', prefix, BinSearchLevelCache.noCache());
    }

    public static BinarySearcherOverPlainSource of(SeekableReadableChannelSource<byte[]> source, Supplier<CacheEntry> cacheSupplier) {
        return new BinarySearcherOverPlainSource(source, cacheSupplier);
    }

    public static BinarySearcherOverPlainSource of(Path path, Supplier<CacheEntry> cacheSupplier) {
        return of(new SeekableReadableChannelSourceOverNio(path), cacheSupplier);
    }

    @Override
    public InputStream search(byte[] prefix) throws IOException {
        CacheEntry cacheEntry = cacheSupplier.get();
        BinSearchLevelCache levelCache = cacheEntry == null ? null : cacheEntry.levelCache();
        if (levelCache == null) {
            levelCache = BinSearchLevelCache.noCache();
        }

        SeekableReadableChannel<byte[]> channel = source.newReadableChannel();
        long searchRangeEnd = source.size();
        InputStream result = BinSearchUtils.configureStream(channel, searchRangeEnd, prefix, levelCache);

        if (source instanceof SeekableReadableSourceWithMonitor<byte[], ?> m) {
            System.err.println(String.format("Total Reads: %d - Total read amount: %d", m.getChannelMonitor().getReadCounter(), m.getChannelMonitor().getReadAmount()));
            // m.getChannelMonitor().dumpJson(System.err);
        }

        return result;
    }


    @Override
    public Stream<ReadableChannelSupplier<byte[]>> parallelSearch(byte[] prefix) throws IOException {
        Stream<ReadableChannelSupplier<byte[]>> result;
        if (prefix == null || prefix.length == 0) {
            result = ReadableChannelSources.splitBySize(source, 50_000_000)
                .map(split -> {
                    return () -> {
                        try {
                            long start = split.getStart();
                            long end = split.getEnd();
                            // System.out.println("start: " + start + " end: " + end);

                            int skipCount = start == 0 ? 0 : 1;
                            ReadableChannel<byte[]> channel = source.newReadableChannel(start);
                            SeekableReadableChannel<byte[]> seekable = (SeekableReadableChannel<byte[]>)channel;
                            channel = new ReadableChannelWithLimitByDelimiter<>(channel, seekable::position, false, (byte)'\n', end);
                            if (skipCount > 0) {
                                channel = new ReadableChannelWithSkipDelimiter<>(channel, (byte)'\n', skipCount);
                            }

                            return channel;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    };
                });
        } else {
            result = BinarySearcher.super.parallelSearch(prefix);
        }
        return result;
    }

    /**
     * When this method returns the input stream's position is unspecified.
     *
     * @param in The seekable input stream on which to perform binary search for the given prefix.
     * @param searchMode Whether we are searching the initial match, or the start or end of a run of matches.
     * @param start
     * @param end
     * @param delimiter
     * @param prefix
     * @return
     * @throws IOException
     */
    public static Match binarySearch(
            SeekableInputStream in, SearchMode searchMode, int depth, long start, long end, long knownDelimPos, byte delimiter, byte[] prefix,
            BinSearchLevelCache cache
            ) throws IOException {
        if (start > end) {
            return null;
        }

        long mid = (start + end) >> 1; // division by 2

        if (false) {
            System.out.println(String.format("%d <= %d < %d)", start, mid, end));
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("%d <= %d < %d)", start, mid, end));
        }

        int cmp = 0;
        // Find the next record start

        long nextDelimPos = cache.getDisposition(mid);
        long bytesToNextDelimiter = -1;

        // Allowed search bytes restricts the search range; we must not read over 'knownDelimPos'.
        long allowedSearchBytes;
        if (nextDelimPos == -1) {
            in.position(mid);

            if (mid > 0) {
                // Example: prefixLength=1 mid=5, end=10 ->
                //          last position where we could find a prefix = 9 -> allwowed range [5, 9] -> 5 bytes
                //          (at offsets 5, 6, 7, 8, 9)
                // -> end - mid - prefixLength + 1
                allowedSearchBytes = knownDelimPos - mid - prefix.length + 1;
                bytesToNextDelimiter = BinSearchUtils.readUntilDelimiter(in, delimiter, allowedSearchBytes);
            }

            // if no delimiter was found the next known delimiter remains knownDelimPos
            if (bytesToNextDelimiter < 0) {
                nextDelimPos = knownDelimPos;
            } else {
                nextDelimPos = mid + bytesToNextDelimiter;
            }
            cache.setDisposition(depth, mid, nextDelimPos);
        }

        // We couldn't find a delimiter after mid - try to search left
        if (nextDelimPos == knownDelimPos) {
            cmp = -1;
        }

        HeaderRecord headerRecord;
        if (cmp == 0) {
            headerRecord = cache.getHeader(nextDelimPos);
            if (headerRecord == null || (headerRecord.data().length < prefix.length && !headerRecord.isDataConsumed())) {
                // If bytesToNextDelimiter is -1 it means that the next delimiter position
                // was taken from cache - we then need to position 'in' to the delimiter position
                if (bytesToNextDelimiter == -1) {
                    in.position(nextDelimPos);
                }

                boolean isDataConsumed = false;
                int blockSize = Math.max(prefix.length, 256);
                byte[] header = new byte[blockSize];
                // XXX resort to a readFully without extra wrapping
                // TODO Input stream may need to be reinitialized
                int n = ReadableChannels.readFully(ReadableChannels.wrap(in), header, 0, blockSize);
                if (n < blockSize) {
                    isDataConsumed = true;
                    header = Arrays.copyOf(header, n);
                }
                headerRecord = new HeaderRecord(nextDelimPos, 0, header, isDataConsumed);
                cache.setHeader(depth, headerRecord);
            }

            // Compare the available bytes; with the condition: compare(prefixByte, EOF) := 1
            if (cmp == 0) {
                int l = Math.min(prefix.length, headerRecord.data().length);
                cmp = Arrays.compare(prefix, 0, l, headerRecord.data(), 0, l);

                // The header was shorter than the prefix
                if (cmp == 0 && l < prefix.length) {
                    cmp = 1;
                }
                if (false) {
                    System.out.println("Compared:");
                    System.out.println("  " + new String(prefix, 0, l, StandardCharsets.UTF_8));
                    System.out.println("  " + new String(headerRecord.data(), 0, l, StandardCharsets.UTF_8));
                    System.out.println("  " + cmp);
                }
            }
        }

        Match result;
        if(cmp == 0) {
            long candidateResult = nextDelimPos;
            long left = candidateResult;
            long right = candidateResult;
            if (SearchMode.LEFT.equals(searchMode) || SearchMode.BOTH.equals(searchMode)) {
                // Find the start of a run:
                // Continue searching left - if there is no match then return the candidate result
                // Match expandLeft = binarySearch(in, SearchMode.LEFT, depth + 1, start, nextDelimPos - 1, delimiter, prefix, cache);
                // Match expandLeft = binarySearch(in, SearchMode.LEFT, depth + 1, start, mid - 1, delimiter, prefix, cache);
                Match expandLeft = binarySearch(in, SearchMode.LEFT, depth + 1, start, mid - 1, nextDelimPos, delimiter, prefix, cache);
                if (expandLeft != null) {
                    left = expandLeft.start();
                }
            }
            // TODO Find the right end when streaming the data - not here
            boolean findEndOfRun = false;
            if (findEndOfRun) {
//                if (SearchMode.RIGHT.equals(searchMode) || SearchMode.BOTH.equals(searchMode)) {
//                    Match expandRight = binarySearch(in, SearchMode.RIGHT, depth + 1, nextDelimPos + 1, end, delimiter, prefix, cache);
//                    if (expandRight != null) {
//                        right = expandRight.end();
//                    }
//                }
            }
            result = new Match(left, right);
        } else if(cmp < 0) {
            // result = binarySearch(in, searchMode, depth + 1, start, nextDelimPos - 1, delimiter, prefix, cache);
            result = binarySearch(in, searchMode, depth + 1, start, mid - 1, nextDelimPos, delimiter, prefix, cache);
        } else {
            result = binarySearch(in, searchMode, depth + 1, nextDelimPos + 1, end, knownDelimPos, delimiter, prefix, cache);
        }

        return result;
    }
}
