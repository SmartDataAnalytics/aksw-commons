package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.hadoop.SeekableInputStreams;
import org.aksw.commons.io.hadoop.binseach.v2.BinSearchResourceCache.CacheEntry;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannelSupplier;
import org.aksw.commons.io.input.ReadableChannelWithLimitByDelimiter;
import org.aksw.commons.io.input.ReadableChannelWithSkipDelimiter;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableSourceWithMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.sansa_stack.io.util.InputStreamWithZeroOffsetRead;

/**
 * Binary search implementation that finds lines matching a prefix in a
 * 'block' source such as a bzip2 compressed file.
 */
public class BinarySearcherOverBlockSource
    implements BinarySearcher
{
    private static final Logger logger = LoggerFactory.getLogger(BinarySearcherOverBlockSource.class);

    protected BlockSource blockSource;
    protected Supplier<CacheEntry> cacheSupplier;

    public BinarySearcherOverBlockSource(BlockSource blockSource, Supplier<CacheEntry> cacheSupplier) {
        super();
        this.blockSource = blockSource;
        this.cacheSupplier = cacheSupplier;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public InputStream search(byte[] prefix) throws IOException {
        CacheEntry cacheEntry = cacheSupplier.get();
        BinSearchLevelCache levelCache = cacheEntry == null ? null : cacheEntry.levelCache();
        if (levelCache == null) {
            levelCache = BinSearchLevelCache.noCache();
        }

        InputStream result;
        Match match = binarySearch(blockSource, prefix, levelCache);
        if (match != null) {
            // System.out.println("Match found: " + match);

            Cache<Long, Block> blockCache = cacheEntry == null ? null : cacheEntry.blockCache();
            if (blockCache == null) {
                blockCache = Caffeine.newBuilder().maximumSize(16).build();
            }

            long startBlockId = match.start();

            SeekableReadableChannelOverBlocks channel = new SeekableReadableChannelOverBlocks(blockSource, startBlockId, blockCache);
            long blockSize = channel.getStartingBlockSize();
            blockSize = 900000;
            result = BinSearchUtils.configureStream(channel, blockSize * 2, prefix, BinSearchLevelCache.noCache());

            // TODO Find the end record in the last block
            // TODO We need to track the start offset of the last block (if known):
            //   Adjust the end offset e to e', then find an preceeding offset p such that p' := adjust(p) and adjust(p' + 1) = e'

            boolean showKnownBlocks = false;
            if (showKnownBlocks) {
                for (Block block : channel.getKnownBlocks()) {
                    System.out.println("BLOCK " + block.getThisBlockId());
                    System.out.println("===============================================");
                    byte[] buffer = new byte[(int)block.getBuffer().size()];
                    block.getBuffer().readInto(buffer, 0, 0, buffer.length);

                    System.err.println(new String(buffer, StandardCharsets.UTF_8));
                }
            }

        } else {
            result = InputStream.nullInputStream();
        }

        if (blockSource.getDelegate() instanceof SeekableReadableSourceWithMonitor<byte[], ?> m) {
            System.err.println(String.format("Total Reads: %d - Total read amount: %d", m.getChannelMonitor().getReadCounter(), m.getChannelMonitor().getReadAmount()));
            // m.getChannelMonitor().dumpJson(System.err);
        }

        return result;
    }

    @Override
    public Stream<ReadableChannelSupplier<byte[]>> parallelSearch(byte[] prefix) throws IOException {
        Stream<ReadableChannelSupplier<byte[]>> result;
        if (prefix == null || prefix.length == 0) {
            result = ReadableChannelSources.splitBySize(blockSource, 50_000_00)
                .map(split -> {
                    return () -> {
                        try {
                            long start = split.getStart();
                            long end = split.getEnd();

                            int skipCount = start == 0 ? 0 : 1;
                            ReadableChannel<byte[]> channel = blockSource.newReadableChannel(start, true);
                            SeekableReadableChannel<byte[]> seekable = (SeekableReadableChannel<byte[]>)channel;
                            channel = new ReadableChannelWithLimitByDelimiter<>(channel, seekable::position, true, (byte)'\n', end);
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

    public static Match binarySearch(BlockSource blockSource, byte[] prefix, BinSearchLevelCache cache) throws IOException {
        long end = blockSource.size();

        long startAfter = adjustStart(blockSource, 1, 0, cache);
        // System.out.println("StartAfter:" + startAfter);

//        long startAfter2 = adjustStart(blockSource, startAfter, 0, cache);
//        // adjustStart(adjustedStart) -> adjustedStart
//        System.out.println("StartAfter2:" + startAfter2 + 1);

        Match result = binarySearch(blockSource, SearchMode.BOTH, 0, 0, startAfter, end, (byte)'\n', prefix, cache);
        return result;
    }

    /** Adjust a position to the next block. It must hold that this function is idempotent: adjustStart(adjustStart(offset)) = adjustStart(offset) */
    public static long adjustStart(BlockSource blockSource, long start, int depth, BinSearchLevelCache cache) throws IOException {
        long currentBlockId = cache.getDisposition(start);
        if (currentBlockId == -1) {
            try (BlockSourceChannel channel = blockSource.newReadableChannel(start)) {
                currentBlockId = channel.getStartingBlockId();
                if (currentBlockId == -1) {
                    throw new IllegalStateException("Should not happen: Block id not set after read.");
                }
                cache.setDisposition(depth, start, currentBlockId);
                // System.out.println("Adjusted: " + start + " -> " + currentBlockId);
            }
        }
        return currentBlockId;
    }

    /**
     * When this method returns the input stream's position is unspecified.
     *
     * <p>
     * Note on cache semantics:
     * <ol>
     *   <li>Disposition is the mapping from the current offset to that of the next block (NOT the record).</li>
     *   <li>The header map maps the block id to the first record</li>
     * </ol>
     *
     * @param in The seekable input stream on which to perform binary search for the given prefix.
     * @param searchMode Whether we are searching the initial match, or the start or end of a run of matches.
     * @param start
     * @param startAfter The adjusted start for the offset (start + 1)
     * @param end
     * @param delimiter
     * @param prefix
     * @return
     * @throws IOException
     */
    public static Match binarySearch(BlockSource blockSource, SearchMode searchMode, int depth, long start, long startAfter, long end, byte delimiter, byte[] prefix, BinSearchLevelCache cache) throws IOException {
        if (start > end) {
            return null;
        }

        long mid = (start + end) >> 1; // division by 2

        // If mid lies within the starting block then adjust it to start
        if (mid > start && mid <= startAfter) {
            mid = startAfter;
        }

        if (false) {
            System.out.println(String.format("%d <= %d < %d)", start, mid, end));
        }

        long nextBlockId;
        int cmp = 0;
        long startBlockId;
        long currentBlockId;


        InputStream in = null; // Initialized on demand
        try {
            if (mid > 0) {
                nextBlockId = cache.getDisposition(mid);
                if (nextBlockId == -1) {
                    BlockSourceChannel channel = blockSource.newReadableChannel(mid);
                    in = new InputStreamWithZeroOffsetRead(SeekableInputStreams.create(channel));

                    // The start blockId is the position such that
                    // blockSource.newReadableChannel(startBlockId + 1) would return the next block
                    startBlockId = channel.getStartingBlockId();
                    currentBlockId = channel.getCurrentBlockId();

                    // System.out.println(String.format("start %s - current %s", startBlockId, currentBlockId));

                    if (startBlockId == -1) {
                        throw new IllegalStateException("Should not happen: Block id not set after read.");
                    }

                    nextBlockId = startBlockId;
                    cache.setDisposition(depth, mid, startBlockId);
                }
            } else {
                nextBlockId = 0;
            }

            HeaderRecord headerRecord = cache.getHeader(nextBlockId);
            int l = prefix.length;
            if (headerRecord == null || (headerRecord.data().length < prefix.length && !headerRecord.isDataConsumed())) {
                int blockSize = Math.max(prefix.length, 256);
                byte[] header = new byte[blockSize];
                if (in == null) {
                    // TODO mid should be replaced by nextBlockId (probably -1)
                    BlockSourceChannel channel = blockSource.newReadableChannel(mid);
                    in = new InputStreamWithZeroOffsetRead(SeekableInputStreams.create(channel));
                }
                long bytesToNextDelimiter = BinSearchUtils.readUntilDelimiter(in, delimiter, Long.MAX_VALUE);
                if (bytesToNextDelimiter < 0) {
                    // If there is no further record then search left
                    cmp = -1;
                } else {
                    boolean isDataConsumed = false;
                    // XXX resort to a readFully without extra wrapping
                    int n = ReadableChannels.readFully(ReadableChannels.wrap(in), header, 0, blockSize);
                    // XXX If there are too few available bytes then the header will always try to read a new header
                    if (n < blockSize) {
                        isDataConsumed = true;
                        header = Arrays.copyOf(header, n);
                    }
                    if (header.length < prefix.length) {
                        cmp = -1;
                    }
                    headerRecord = new HeaderRecord(nextBlockId, (int)bytesToNextDelimiter, header, isDataConsumed);
                    cache.setHeader(depth, headerRecord);
                }
            }
            if (cmp == 0) {
                cmp = Arrays.compare(prefix, 0, l, headerRecord.data(), 0, l);
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("  Compared: %s %s %s",
                            new String(prefix, 0, l, StandardCharsets.UTF_8),
                            (cmp < 0 ? "<" : cmp > 0 ? ">" : "=="),
                            new String(headerRecord.data(), 0, l, StandardCharsets.UTF_8)));
                }

            }
//            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        Match result;
        if(cmp == 0) {
            // long candidateResult = startBlockId;
            long candidateResult = nextBlockId;
            long left = candidateResult;
            long right = candidateResult;
            if (SearchMode.LEFT.equals(searchMode) || SearchMode.BOTH.equals(searchMode)) {
                // Find the start of a run:
                // Continue searching left - if there is no match then return the candidate result
                if (nextBlockId != 0) {
                    if (mid != start) { // We can't expand if we are already at start
                        long nextEnd = mid <= startAfter ? start : mid;
                        Match expandLeft = binarySearch(blockSource, SearchMode.LEFT, depth, start, startAfter, nextEnd, delimiter, prefix, cache);
                        if (expandLeft != null) {
                            left = expandLeft.start();
                        }
                    }
                }
            }
            boolean findEndOfRun = true;
            if (findEndOfRun) {
                if (SearchMode.RIGHT.equals(searchMode) || SearchMode.BOTH.equals(searchMode)) {
                    long nextStart = nextBlockId + 1;

                    // Don't expand beyond end
                    if (nextStart <= end) {
                        long nextStartAfter = adjustStart(blockSource, nextStart + 1 , depth, cache);
                        Match expandRight = binarySearch(blockSource, SearchMode.RIGHT, depth, nextStart, nextStartAfter, end, delimiter, prefix, cache);
                        if (expandRight != null) {
                            right = expandRight.end();
                        }
                    }
                }
            }
            result = new Match(left, right);
        } else if(cmp < 0) {
            // long nextEnd = mid; //nextBlockId - 1;
            if (mid == start) {
                result = new Match(start, start);
            } else if (mid <= startAfter) {
                // set end to start
                result = binarySearch(blockSource, searchMode, depth, start, startAfter, start, delimiter, prefix, cache);
            } else {
                result = binarySearch(blockSource, searchMode, depth, start, startAfter, mid, delimiter, prefix, cache);
            }
        } else {
            // If the prefix compares greater than the header it may still be in the starting page
            // so do not increment the start
            long nextStart = nextBlockId;

            // if (nextStart + 1 >= end) {
            // if (startAfter + 1 >= end) {
            if (end <= startAfter) {
                // If the end marker is within the starting block then return the starting block
                result = new Match(start, start);
            } else {
                long nextStartAfter = adjustStart(blockSource, nextStart + 1 , depth, cache);
                result = binarySearch(blockSource, searchMode, depth, nextStart, nextStartAfter, end, delimiter, prefix, cache);
            }
        }

        return result;
    }
}
