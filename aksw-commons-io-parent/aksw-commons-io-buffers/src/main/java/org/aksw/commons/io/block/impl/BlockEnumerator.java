package org.aksw.commons.io.block.impl;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.util.ref.Ref;

/**
 * A helper that holds a reference to the current block
 * and automatically closes it when advancing to the next one.
 * *
 * IterState.advance();
 * IterState.closeCurrent();
 * IterState.current();
 *
 * @author raven
 *
 */
public class BlockEnumerator {
    public Ref<? extends Block> blockRef;
    public Block block;
    public Seekable seekable;

    protected boolean yieldSelf;
    protected boolean skipFirstClose;
    protected boolean isFwd;

    protected BlockEnumerator(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable, boolean isFwd, boolean skipFirstClose) {
        Objects.requireNonNull(blockRef);

        this.blockRef = blockRef;
        this.block = blockRef.get();
        this.seekable = seekable;

        this.yieldSelf = yieldSelf;
        this.skipFirstClose = skipFirstClose;
        this.isFwd = isFwd;
    }

    public Ref<? extends Block> getCurrentBlockRef() {
        return blockRef;
    }

    public Block getCurrentBlock() {
        return block;
    }

    public Seekable getCurrentSeekable() {
        return seekable;
    }

    public boolean hasNext() {
        boolean result;
        try {
            result = yieldSelf // Return the block initial block first
                ? true
                :isFwd
                    ? block.hasNext()
                    : block.hasPrev();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public void closeCurrent() {
        if(!skipFirstClose) {
            try {
                if (!blockRef.isClosed()) {
                    blockRef.close();
                }

                if (seekable.isOpen()) {
                    seekable.close();
                }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Move to the next or previous block based on the configured direction. */
    public void advance() {
        try {
            if(yieldSelf) {
                yieldSelf = false;
            } else {
                Ref<? extends Block> next = isFwd
                        ? block.nextBlock()
                        : block.prevBlock();

                if(next == null) {
                    // nothing to do
                } else {
                    closeCurrent();
                    skipFirstClose = false;

                    blockRef = next;
                    block = next.get();
                    seekable = block.newChannel();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockEnumerator fwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable) {
        return new BlockEnumerator(yieldSelf, blockRef, seekable, true, true);
    }

    public static BlockEnumerator fwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable, boolean skipFirstClose) {
        return new BlockEnumerator(yieldSelf, blockRef, seekable, true, skipFirstClose);
    }

    public static BlockEnumerator fwd(boolean yieldSelf, Ref<? extends Block> blockRef, boolean skipFirstClose) {
        return new BlockEnumerator(yieldSelf, blockRef, blockRef.get().newChannel(), true, skipFirstClose);
    }

    public static BlockEnumerator bwd(boolean yieldSelf, Ref<? extends Block> blockRef, Seekable seekable) {
        return new BlockEnumerator(yieldSelf, blockRef, seekable, false, true);
    }
}
