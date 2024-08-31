package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.hadoop.SeekableInputStreams;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannelSupplier;
import org.aksw.commons.io.input.ReadableChannelWithLimitByDelimiter;
import org.aksw.commons.io.input.ReadableChannelWithSkipDelimiter;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.sansa_stack.io.util.InputStreamWithZeroOffsetRead;


public class BinarySearcherOverBlockSource
    implements BinarySearcher
{
    private static final Logger logger = LoggerFactory.getLogger(BinarySearcherOverBlockSource.class);

    protected BlockSource blockSource;
    protected BinSearchLevelCache cache;
    protected Cache<Long, Block> pageCache;

    public BinarySearcherOverBlockSource(BlockSource blockSource, BinSearchLevelCache cache, int pageCacheSize) {
        this(blockSource, cache, Caffeine.newBuilder().maximumSize(pageCacheSize).build());
    }

    public BinarySearcherOverBlockSource(BlockSource blockSource, BinSearchLevelCache cache, Cache<Long, Block> pageCache) {
        super();
        this.blockSource = blockSource;
        this.cache = cache;
        this.pageCache = pageCache;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public InputStream search(byte[] prefix) throws IOException {
        InputStream result;
        Match match = binarySearch(blockSource, prefix, cache);
        if (match != null) {
            // System.out.println("Match found: " + match);

            long startBlockId = match.start();

            SeekableReadableChannelOverBlocks channel = new SeekableReadableChannelOverBlocks(blockSource, startBlockId, pageCache);
            long blockSize = channel.getStartingBlockSize();
            blockSize = 900000;
            result = BinSearchUtils.configureStream(channel, blockSize * 2, prefix);

            boolean showKnownBlocks = false;
            if (showKnownBlocks) {
                for (Block block : channel.getKnownBlocks()) {
                    System.out.println("BLOCK " + block.getThisBlockId());
                    System.out.println("===============================================");
                    byte[] buffer = new byte[(int)block.getBuffer().size()];
                    block.getBuffer().readInto(buffer, 0, 0, buffer.length);

                    System.out.println(new String(buffer, StandardCharsets.UTF_8));
                }
            }

        } else {
            result = InputStream.nullInputStream();
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
        Match result = binarySearch(blockSource, SearchMode.BOTH, 0, 0, end, (byte)'\n', prefix, cache);
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
    public static Match binarySearch(BlockSource blockSource, SearchMode searchMode, int depth, long start, long end, byte delimiter, byte[] prefix, BinSearchLevelCache cache) throws IOException {
        if (start > end) {
            return null;
        }

        long mid = (start + end) >> 1; // division by 2

        if (false) {
            System.out.println(String.format("%d <= %d < %d)", start, mid, end));
        }

        long nextBlockId;
        int cmp = 0;
        long startBlockId;
        long currentBlockId;


        InputStream in = null; // Created on demand
        try {
            if (mid > 0) {
                nextBlockId = cache.getDisposition(mid);
                if (nextBlockId == -1) {
                    BlockSourceChannelAdapter channel = blockSource.newReadableChannel(mid);
                    in = new InputStreamWithZeroOffsetRead(SeekableInputStreams.create(channel));

                    startBlockId = channel.getStartingBlockId();
                    currentBlockId = channel.getCurrentBlockId();

                    long effectiveBlockId = startBlockId;

                    // System.out.println(String.format("start %s - current %s", startBlockId, currentBlockId));

                    if (effectiveBlockId == -1) {
                        throw new IllegalStateException("Should not happen: Block id not set after read.");
                    }

//                    if (effectiveBlockId > end) {
//                        // nextBlockId = mid;
//                        nextBlockId = effectiveBlockId;
//                        cmp = -1;
//                    }
//                    else {
//                        nextBlockId = effectiveBlockId;
//                    }

                    nextBlockId = effectiveBlockId;
                    cache.setDisposition(depth, mid, effectiveBlockId);
                }
            } else {
                nextBlockId = 0;
            }

//            if (cmp == 0) {
            HeaderRecord headerRecord = cache.getHeader(nextBlockId);
            int l = prefix.length;
            if (headerRecord == null || (headerRecord.data().length < prefix.length && !headerRecord.isDataConsumed())) {
                int blockSize = Math.max(prefix.length, 256);
                byte[] header = new byte[blockSize];
                if (in == null) {
                    // TODO mid should be replaced by nextBlockId (probably -1)
                    BlockSourceChannelAdapter channel = blockSource.newReadableChannel(mid);
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
                    Match expandLeft = binarySearch(blockSource, SearchMode.LEFT, depth, start, mid, delimiter, prefix, cache);
                    if (expandLeft != null) {
                        left = expandLeft.start();
                    }
                }
            }
            boolean findEndOfRun = false;
            if (findEndOfRun) {
                if (SearchMode.RIGHT.equals(searchMode) || SearchMode.BOTH.equals(searchMode)) {
                    Match expandRight = binarySearch(blockSource, SearchMode.RIGHT, depth, nextBlockId + 1, end, delimiter, prefix, cache);
                    if (expandRight != null) {
                        right = expandRight.end();
                    }
                }
            }
            result = new Match(left, right);
        } else if(cmp < 0) {
            long nextEnd = mid; //nextBlockId - 1;
            if (start >= nextEnd) {
                result = new Match(start, start);
            } else {
                result = binarySearch(blockSource, searchMode, depth, start, nextEnd, delimiter, prefix, cache);
            }
        } else {
            // If the prefix compares greater than the header it may still be in the starting page
            // so do not increment the start
            long nextStart = nextBlockId;
            if (nextStart + 1>= end) {
                result = new Match(start, start);
            } else {
                result = binarySearch(blockSource, searchMode, depth, nextStart, end, delimiter, prefix, cache);
            }
        }

        return result;
    }
}
