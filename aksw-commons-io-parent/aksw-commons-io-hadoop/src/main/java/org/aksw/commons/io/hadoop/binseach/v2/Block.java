package org.aksw.commons.io.hadoop.binseach.v2;

import org.aksw.commons.io.buffer.plain.Buffer;

import com.google.common.primitives.Ints;

public class Block {
    /** A physical offset in the block source - position in the encoded/compressed data. */
    protected long thisBlockId;

    /** A physical offset in the block source - position in the encoded/compressed data. */
    protected long nextBlockId;

    protected Buffer<byte[]> buffer;
    protected BlockSource blockSource;

    public Block(BlockSource blockSource, Buffer<byte[]> buffer, long thisBlockId, long nextBlockId) {
        super();
        this.blockSource = blockSource;
        this.buffer = buffer;
        this.thisBlockId = thisBlockId;
        this.nextBlockId = nextBlockId;

        if (nextBlockId == thisBlockId) {
            throw new IllegalArgumentException(String.format("This and next block ids are the same %d - %d", thisBlockId, nextBlockId));
        }
    }

    public Block(Buffer<byte[]> buffer, long thisBlockId, long nextBlockId) {
        this(null, buffer, thisBlockId, nextBlockId);
    }

//    public BlockSource getBlockSource() {
//        return blockSource;
//    }

    public Buffer<byte[]> getBuffer() {
        return buffer;
    }

    public long getNextBlockId() {
        return nextBlockId;
    }

    public long getThisBlockId() {
        return thisBlockId;
    }

    // Blocks should fit into arrays, so int should be fine
    // Blocks are assumed to be in memory, so IO errors should not occur.
    public int size() {
        try {
            return Ints.checkedCast(buffer.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
