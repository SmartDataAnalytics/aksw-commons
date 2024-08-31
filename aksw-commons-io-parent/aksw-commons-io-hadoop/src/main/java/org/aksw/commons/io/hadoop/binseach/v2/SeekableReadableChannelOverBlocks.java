package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.buffer.plain.BufferOverArray;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.primitives.Ints;

public class SeekableReadableChannelOverBlocks
    extends SeekableReadableChannelBase<byte[]>
{
    private static final Logger logger = LoggerFactory.getLogger(SeekableReadableChannelOverBlocks.class);

    protected BlockSource blockSource;
    protected BlockSourceChannelAdapter channel; // = blockSource.newReadableChannel(startBlockId, true);

    // The first block id - logical positioning starts at 0 relative to the beginning of this block
    protected long firstBlockId;

    protected long currentBlockLogicalOffset;
    protected Block currentBlock;

    protected long currentBlockId;
    protected long currentLogicalPos;

    protected Cache<Long, Block> globalCache;

    // protected NavigableMap<Long, Block> logicalPosToBlock;
    protected NavigableMap<Long, Block> blockIdToBlock;
    protected NavigableMap<Long, Long> logicalPosToBlockId;

//     protected NavigableMap<Long, Block> blockOffsets;
    // protected Set<Long> blockAccessHistory;
    // protected int maxBlockAccessHistorySize;

    public Collection<Block> getKnownBlocks() {
        return blockIdToBlock.values();
    }

    /**
     *
     * @param blockSource
     * @param channel The initial channel positioned at the beginning of a block.
     * @param globalCache
     */
    public SeekableReadableChannelOverBlocks(BlockSource blockSource, long firstBlockId, Cache<Long, Block> globalCache) {
        super();
        this.blockSource = blockSource;
        this.firstBlockId = firstBlockId;
        this.channel = null;
        // this.currentBlockId = channel.getCurrentBlockId();
        this.globalCache = globalCache;


        this.blockIdToBlock = new TreeMap<>();
        this.logicalPosToBlockId = new TreeMap<>();


        // this.maxBlockAccessHistorySize = 5;
        // this.blockAccessHistory = new LinkedHashSet<>(maxBlockAccessHistorySize);
    }

    @Override
    public SeekableReadableChannel<byte[]> cloneObject() {
        throw new UnsupportedOperationException();
    }

//    protected long getLastLoadedLogicalPos() {
//        Entry<Long, Block> lastEntry = logicalPosToBlock.lastEntry();
//        long lastLoadedPosition = lastEntry == null
//            ? 0
//            :lastEntry.getKey() + lastEntry.getValue().size();
//        return lastLoadedPosition;
//    }

    protected boolean isPosValidInBlock() {
        boolean result = currentLogicalPos >= currentBlockLogicalOffset && currentLogicalPos < currentBlockLogicalOffset + currentBlock.size();
        return result;
    }

    /**
     *
     */
    protected void ensureCurrentBlock() {
        // Sanity check
        if (currentLogicalPos < 0) {
            throw new RuntimeException("Negative logical position - should not happen");
        }

        Block block = null;
        long logicalBlockOffset;
        long blockId;

        while (currentBlock == null || !isPosValidInBlock()) {
            // Find the entry in logicalPosToBlockId that is closest to the current logical position
            // From there iterate the blocks until we reach the one which contains the logical position
            NavigableMap<Long, Long> headMap = logicalPosToBlockId.headMap(currentLogicalPos, true).descendingMap();
            Iterator<Entry<Long, Long>> it = headMap.entrySet().iterator();

            Entry<Long, Long> logicalBlockOffsetAndId = it.hasNext() ? it.next() : null; // logicalPosToBlockId.floorEntry(currentLogicalPos);
            if (logicalBlockOffsetAndId == null) {
                blockId = firstBlockId;
                logicalBlockOffset = 0;
            } else {
                blockId = logicalBlockOffsetAndId.getValue();
                logicalBlockOffset = logicalBlockOffsetAndId.getKey();
            }
            block = getOrLoadBlock(blockId, logicalBlockOffset);

            int blockSize = block == null ? -1 : block.size();
            long nextLogicalBlockOffset = logicalBlockOffset + blockSize;

            if (blockSize != -1 && (currentLogicalPos >= logicalBlockOffset && currentLogicalPos < nextLogicalBlockOffset)) {
                // We found a block that contains the current logical position
                currentBlock = block;
                currentBlockId = blockId;
                currentBlockLogicalOffset = logicalBlockOffset;
                break;
            } else {
                // The found block's logical end still lies before the current position
                // Use it to the declare where the next block can be found
                long nextBlockId = block.getNextBlockId();
                if (nextBlockId == -1) {
                    currentBlock = null;
                    currentBlockId = -1;
                    currentBlockLogicalOffset = -1;
                    break;
                }

                logicalPosToBlockId.put(nextLogicalBlockOffset, nextBlockId);
            }
        }
    }

    public long getStartingBlockSize() {
        Block block = getOrLoadBlock(firstBlockId, 0);
        return block.size();
    }

    protected Block getOrLoadBlock(long blockId, long logicalBlockOffset) {

        // XXX If a block was reused then close the channel
        // XXX The assumption is that re-opening a channel is cheaper than decompressing a page
        //     We need to evaluate whether this is the case (for local bzip2 it likely is)
        Block block = blockIdToBlock.computeIfAbsent(blockId, bid -> {
            // Get or create from the global cache next block
            Block r = globalCache.get(bid, blkId2 -> {
                try {
                    // If possible then reuse the currently open channel
                    if (channel != null && channel.getCurrentBlockId() != blockId) {
                        // System.err.println("Closing channel with block id: " + channel.getCurrentBlockId() + " requested blockid: " + blockId);
                        channel.close();
                        channel = null;
                    }

                    if (channel == null) {
                        //System.err.println("Opening channel with block id: " +  blockId);
                        channel = blockSource.newReadableChannel(blockId, true);
                    }

                    Block s = loadBlock(blockSource, channel, blkId2);
                    return s;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            logicalPosToBlockId.put(logicalBlockOffset, blockId);
            return r;
        });


        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Got/loaded blockId %d of size %d - followed by: %d", blockId, block.size(), block.getNextBlockId()));
        }

        // long nextBlockId = channel.getCurrentBlockId();
        // System.out.println("next: " + nextBlockId);

        // boolean hasReachedEnd = nextBlockId == blockId;
//        if (!hasReachedEnd) {
//            currentBlock = block;
//            currentBlockId = nextBlockId;
//        }
        return block;
    }
//
//    protected boolean loadNextBlock() {
//        // Load next block
//        Block block = globalCache.get(currentBlockId, s -> {
//            try {
//                return loadBlock(blockSource, channel, currentBlockId);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        System.out.println(String.format("%d %d", currentBlockId, block.size()));
//
//        long nextBlockId = channel.getCurrentBlockId();
//        System.out.println("next: " + nextBlockId);
//
//        boolean hasReachedEnd = nextBlockId == currentBlockId;
//        if (!hasReachedEnd) {
//            currentBlock = block;
//            currentBlockId = nextBlockId;
//        }
//        return hasReachedEnd;
//    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {

        int result;
        while (true) {
            ensureCurrentBlock();
            if (currentBlock == null) {
                result = -1;
                break;
            }

            int positionInBlock = Ints.checkedCast(currentLogicalPos - currentBlockLogicalOffset);
            int remaining = currentBlock.size() - positionInBlock;

            if (remaining > 0) {
                int requestLen = Math.min(remaining, length);
                result = currentBlock.getBuffer().readInto(array, position, positionInBlock, requestLen);
                break;
            } else {
                // ensureCurrentBlock() must ensure there is sufficient remaining data
                throw new RuntimeException("should not happen");
            }
        }

        if (result > 0) {
            currentLogicalPos += result;
        }

        return result;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public long position() throws IOException {
        return currentLogicalPos;
    }

    @Override
    protected void closeActual() throws Exception {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        super.closeActual();
    }

    @Override
    public void position(long pos) throws IOException {
        if (pos < 0) {
            throw new IllegalArgumentException("Negative position: " + pos);
        }
        currentLogicalPos = pos;
        currentBlock = null;
        currentBlockId = -1;
        currentBlockLogicalOffset = -1;
    }

    public static Block loadBlock(BlockSource blockSource, BlockSourceChannelAdapter channel, long thisBlockId) throws IOException {
        ByteArrayOutputStream blockBytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4 * 1024];

        while (true) {
            int n;
            while ((n = channel.read(buffer, 0, buffer.length)) > 0) {
                blockBytes.write(buffer, 0, n);
                // System.out.println("Current block id: " + channel.getCurrentBlockId());
            }

            if (n == -1) {
                break;
            } if (n == -2) {
                int blockSize = blockBytes.size();
                if (blockSize == 0) {
                    continue;
                } else {
                    // continue;
                    break;
                }
            }
        }
        byte[] data = blockBytes.toByteArray();
        Buffer<byte[]> buf = BufferOverArray.create(ArrayOps.BYTE, data);
        long nextBlockId = channel.getCurrentBlockId();
        // long nextBlockId = channel.getStartingBlockId();
        // long thisBlockId = channel.getStartingBlockId();
        // long nextBlockId = channel.getCurrentBlockId();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Loaded blockId %d of size %d - followed by blockId %d", thisBlockId, blockBytes.size(), nextBlockId));
        }

        if (thisBlockId == nextBlockId) {
            // End of blocks reached
            nextBlockId = -1;
        }

        Block result = new Block(blockSource, buf, thisBlockId, nextBlockId);
        return result;
    }
}
