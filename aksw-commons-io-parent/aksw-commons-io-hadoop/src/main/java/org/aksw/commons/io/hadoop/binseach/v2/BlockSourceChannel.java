package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.hadoop.ReadableChannelWithBlockAdvertisement;
import org.aksw.commons.io.hadoop.SeekableInputStream;
import org.aksw.commons.io.hadoop.SeekableInputStreams;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannels;
import org.apache.hadoop.io.compress.SplitCompressionInputStream;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.io.compress.SplittableCompressionCodec.READ_MODE;

public class BlockSourceChannel
    // implements SeekableByteChannel
    implements SeekableReadableChannel<byte[]>
{
    protected final SplittableCompressionCodec codec;
    protected final SeekableReadableChannel<byte[]> encodedChannel;

    /** The decoded channel is recreated on every first read after calling {@link #position(long)}. */
    protected long decodedChannelStart;

    /** The current non-seekable channel. */
    // protected ReadableChannelWithBlockAdvertisement decodedChannel;
    protected ReadableChannelWithBlockAdvertisement decodedChannel;

    /** The starting block id will be set after reading the first byte from the stream after its creation or setting the position. */
//    protected long startingBlockId;
//
//    protected long currentBlockId;
//    protected long nextBlockId;

    protected int positionInBlock;

    protected ByteBuffer pendingRead = ByteBuffer.allocate(1);
    protected SplitCompressionInputStream decodedIn;
    protected boolean blockMode;

    public BlockSourceChannel(SeekableReadableChannel<byte[]> seekable, SplittableCompressionCodec codec, boolean blockMode) {
        super();
        this.codec = codec;
        this.encodedChannel = seekable;
        this.blockMode = blockMode;
        resetTracker();
    }

    public void ensureDecodedChannel() throws IOException {
        if (decodedChannel == null) {
            // SeekableByteChannel closeShieldedChannel = new SeekableByteChannelWithCloseShield(encodedChannel);
            SeekableReadableChannel<byte[]> closeShieldedChannel = SeekableReadableChannels.closeShield(encodedChannel);
            SeekableInputStream seekableIn = SeekableInputStreams.create(closeShieldedChannel); // closeShieldedChannel, SeekableByteChannel::position, SeekableByteChannel::position);
            decodedIn = codec.createInputStream(seekableIn, null, decodedChannelStart, Long.MAX_VALUE, READ_MODE.BYBLOCK);
            // System.out.println("decodedIn - pos: " + decodedIn.getPos());
//            startingBlockId = decodedIn.getPos();
//            currentBlockId = startingBlockId;
            decodedChannel = SeekableInputStreams.advertiseEndOfBlock(decodedIn);
        }
    }

    protected void resetTracker() {
//        startingBlockId = -1;
//        currentBlockId = -1;
//        nextBlockId = -1;
        positionInBlock = 0;
        pendingRead.position(1);
    }

    public long getStartingBlockId() throws IOException {
        ensureDecodedChannel();
        return decodedChannel.getStartPos();
    }

    public long getCurrentBlockId() throws IOException {
        ensureDecodedChannel();
        return decodedChannel.getCurrentPos();
    }

    public int getPositionInBlock() {
        return positionInBlock;
    }

    public boolean adjustToNextBlock() throws IOException {
        return decodedChannel.adjustToNextBlock();
    }

    protected void closeDecodedChannel() throws IOException {
        if (decodedChannel != null) {
            decodedChannel.close();
            decodedChannel = null;
        }
        resetTracker();
    }

//    @Override
//    public long size() throws IOException {
//        return encodedChannel.size();
//    }

    @Override
    public void close() throws IOException {
        closeDecodedChannel();
        encodedChannel.close();
    }

    @Override
    public boolean isOpen() {
        return encodedChannel.isOpen();
    }

//
//    @Override
//    public int read(ByteBuffer dst) throws IOException {
//        ensureDecodedChannel();
//        int result = blockMode
//                ? readWithBlockEnd(dst)
//                : readWithoutBlockEnd(dst);
//        return result;
//    }
//
//
//    protected int readWithBlockEnd(ByteBuffer dst) throws IOException {
//    	return
//    }
//
//    protected int readWithoutBlockEnd(ByteBuffer dst) throws IOException {
//    }



//    @Override
//    public int read(ByteBuffer dst) throws IOException {
//        ensureDecodedChannel();
//        int result;
//        result = decodedChannel.read(dst);
//        if (!blockMode && result == -2) {
//            result = decodedChannel.read(dst);
//            if (result == -2) {
//                throw new RuntimeException("Consecutive block ends");
//            }
//        }
//
//        return result;
//    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {
        ensureDecodedChannel();
        int result;
        result = decodedChannel.read(array, position, length);
        if (!blockMode && result == -2) {
            result = decodedChannel.read(array, position, length);
            if (result == -2) {
                throw new RuntimeException("Consecutive block ends");
            }
        }
        return result;
    }

//    public int readOld(ByteBuffer dst) throws IOException {
//        ensureDecodedChannel();
//
////        if (positionInBlock == 897025) {
////            System.err.println("HERE");
////        }
//
////        if(blockMode) {
////            System.out.println("BLOCK MODE");
////        }
//
//        boolean doNotEmitBlockEnd = !blockMode;
//
//        int result;
//        if (pendingRead.remaining() > 0) {
//            dst.duplicate().put(pendingRead);
//            // dst.put(pendingRead);
//            result = 1;
//        } else {
//
////        int result = decodedChannel.read(dst);
////        if (currentBlockId == -1 || result == -2) {
////            long nextBlockId = encodedChannel.position();
////            currentBlockId = nextBlockId;
////
////            if (startingBlockId == -1) {
////                startingBlockId = currentBlockId;
////            }
////        }
////        return result;
//
//            boolean didTransition = false;
//            result = 0;
//            while (result == 0) {
//                if (didTransition) {
//                    pendingRead.position(0);
//                    int n = decodedChannel.read(pendingRead);
//
//                    // long nextNextBlockId = decodedChannel.getCurrentPos(); //encodedChannel.position();
//
//                    // System.out.println("STATUS: " + decodedIn.getPos() + " - " + nextNextBlockId);
//                    // currentBlockId = nextBlockId;
//                    // positionInBlock = 0;
//
////                    if (startingBlockId == -1) {
////                        startingBlockId = nextNextBlockId; // This may already be the next block
////                        currentBlockId = nextNextBlockId;
////                        nextBlockId = nextNextBlockId;
////                    } else {
////                        currentBlockId = nextNextBlockId;
////                        nextBlockId = nextNextBlockId;
////                    }
//
//                    if (n == -1) {
//                        result = -1;
//                    } else if (n == -2) {
//                        result = -2;
//                        if (!doNotEmitBlockEnd) {
//                            break;
//                        }
//                    } else if (n == 1) {
//                        if (doNotEmitBlockEnd || !didTransition) {
//                            pendingRead.position(0);
//                            // dst.duplicate().put(pendingRead);
//                            dst.put(pendingRead);
//                            result = n;
//                        } else {
//                            result = -2;
//                            break;
//                        }
//                    }
//
//                } else {
//                    result = decodedChannel.read(dst);
//                }
//
//                if (result > 0) {
//                } else if (result == -1) {
//                    // Nothing to do
//                } else if (result == -2) {
//                    if (didTransition) {
//                        throw new RuntimeException("Consecutive block ends.");
//                    }
//                    didTransition = true;
//                    result = 0;
//                    continue;
//                } else if (result == 0) {
//                    throw new RuntimeException("Zero-byte read.");
//                } else {
//                    throw new RuntimeException("Unknown negative value: " + result);
//                }
//
//                break;
//            }
//        }
//
//        if (result > 0) {
//            positionInBlock += result;
//        }
//        // System.out.println("POS IS BLOCK: " + positionInBlock);
//        return result;
//    }

    @Override
    public long position() throws IOException {
        return decodedChannel == null ? decodedChannelStart : decodedChannel.position();
    }

    @Override
    public void position(long newPosition) throws IOException {
        closeDecodedChannel();
        this.decodedChannelStart = newPosition;
        // encodedChannel.position(newPosition);
        //return this;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public SeekableReadableChannel<byte[]> cloneObject() {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public SeekableByteChannel truncate(long size) throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int write(ByteBuffer src) throws IOException {
//        throw new UnsupportedOperationException();
//    }
}
