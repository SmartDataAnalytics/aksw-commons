package org.aksw.commons.io.hadoop.binseach.bz2;

import java.io.IOException;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.io.util.channel.ChannelFactory;
import org.aksw.commons.util.ref.Ref;

public class DecodedDataBlock
    implements Block
{
    protected BlockSource blockSource;
    protected long blockStart;
    protected ChannelFactory<Seekable> channelFactory;

    @Override
    public boolean hasNext() throws IOException {
        return blockSource.hasBlockAfter(blockStart);
    }

    @Override
    public boolean hasPrev() throws IOException {
        return blockSource.hasBlockBefore(blockStart);
    }

    @Override
    public Ref<? extends Block> nextBlock() throws IOException {
        return blockSource.contentAtOrAfter(blockStart, false);
    }

    @Override
    public Ref<? extends Block> prevBlock() throws IOException {
        return blockSource.contentAtOrBefore(blockStart, false);
    }

    @Override
    public long getOffset() {
        return blockStart;
    }

    public DecodedDataBlock(
            BlockSource blockSource,
            long blockStart,
            ChannelFactory<Seekable> channelFactory) {
        super();
        this.blockSource = blockSource;
        this.blockStart = blockStart;
        this.channelFactory = channelFactory;
    }

    public BlockSource getBufferSource() {
        return blockSource;
    }

    public long getBlockStart() {
        return blockStart;
    }

    public ChannelFactory<Seekable> getChannelFactory() {
        return channelFactory;
    }

    @Override
    public Seekable newChannel() {
        return channelFactory.newChannel();
    }

    @Override
    public void close() throws Exception {
        channelFactory.close();
    }

    @Override
    public long length() throws IOException {
        long result = blockSource.getSizeOfBlock(blockStart);
        return result;
    }
}
