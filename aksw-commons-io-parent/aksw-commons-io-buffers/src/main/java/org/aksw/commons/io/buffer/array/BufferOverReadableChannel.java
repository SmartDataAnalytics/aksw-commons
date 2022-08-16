package org.aksw.commons.io.buffer.array;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.buffer.plain.SubBuffer;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.shared.ChannelBase;
import org.apache.commons.io.input.BoundedInputStream;

import com.google.common.base.Stopwatch;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

/**
 * Implementation of a buffer that supports reading data from a channel.
 * This implementation can serve as a plain buffer by simply providing a channel that provides no data.
 *
 * Instances of these class are thread safe, but the obtained channels are not; each channel should only be operated on
 * by one thread.
 *
 * This class can be seen as a generalization of BufferedInputStream.
 *
 * Differences to BufferedInputStream:
 * - this class caches all data read from the inputstream hence there is no mark / reset mechanism
 * - buffer is split into buckets (no data copying required when allocating more space)
 * - data is loaded on demand based on (possibly concurrent) requests to the seekable channels obtained with
 *   newChannel()
 *
 * Closest known-to-me Hadoop counterpart is BufferedFSInputStream (which is based on BufferedInputStream)
 *
 * @author raven
 *
 */
@ThreadSafe
public class BufferOverReadableChannel<A>
    implements Buffer<A>
{
    /** The buffered data */
    protected A[] buckets;

    protected ArrayOps<A> arrayOps;

    /**
     * End marker with two components (idx, pos)
     *
     * it is wrapped in an object to enable atomic replacement of the reference
     * The pointer is monotonous in the sense that the end marker's logical linear location is only increased
     * Reading an old version while a new one has been set will only cause a read
     * to return on the old boundary, but a subsequent synchronized check for whether loading
     * of additional data is needed is then made anyway
     */
    protected BucketPointer activeEnd;

    /** The number of cached bytes. Corresponds to the linear representation of activeEnd.  */
    protected long knownDataSize = 0;

    protected ReadableChannel<A> dataSupplier;
    protected boolean isDataSupplierConsumed = false;

    protected int minReadSize;

    /** Maximum number to read from the dataSupplier in one request */
    protected int maxReadSize;

    protected Buffer<A> bufferView = new BufferView();

    protected class BufferView implements SubBuffer<A> {
        @Override
        public Buffer<A> getBackend() {
            return BufferOverReadableChannel.this;
        }

        @Override
        public long getStart() {
            return 0;
        }

        @Override
        public long getLength() {
            return knownDataSize;
        }
    }

    public long getKnownDataSize() {
        return knownDataSize;
    }

    /**
     * Returns a view of the buffered data only.
     * Reading from that buffer must never consume data from the data supplier. */
    public Buffer<A> getBuffer() {
        return bufferView;
    }

    /**
     * Return the data supplier
     */
    public ReadableChannel<A> getDataSupplier() {
        return dataSupplier;
    }

    public static <A> long getPosition(ArrayOps<A> arrayOps, A[] buckets, int idx, int pos) {
        long result = 0;
        for(int i = 0; i < idx; ++i) {
            result += arrayOps.length(buckets[i]);
        }

        result += pos;
        return result;
    }

    public static class BucketPointer {
        public BucketPointer(int idx, int pos) {
            super();
            this.idx = idx;
            this.pos = pos;
        }

        int idx;
        int pos;
        @Override
        public String toString() {
            return "BucketPointer [idx=" + idx + ", pos=" + pos + "]";
        }
    }


    /**
     *
     *
     * @param buckets
     * @param pos
     * @return Pointer to a valid location in the know data block or null
     */
    public static <A> BucketPointer getPointer(ArrayOps<A> arrayOps, A[] buckets, BucketPointer end, long pos) {
        long tmp = pos;
        int i;

        int eidx = end.idx;
        int epos = end.pos;
        for(i = 0; i < eidx; ++i) {
            A bucket = buckets[i];
            int n = arrayOps.length(bucket);
            long r = tmp - n;
            if(r < 0) {
                break;
            } else {
                tmp -= n;
            }
        }

        BucketPointer result = i == end.idx && tmp > epos
                ? null
                : new BucketPointer(i, Ints.checkedCast(tmp));
        return result;
    }


    public BufferOverReadableChannel(
            ArrayOps<A> arrayOps,
            int initialBucketSize,
            ReadableChannel<A> dataSupplier) {
        if (initialBucketSize <= 0) {
            throw new IllegalArgumentException("Bucket size must not be 0");
        }

        this.arrayOps = arrayOps;
        this.buckets = (A[])new Object[8];
        buckets[0] = arrayOps.create(initialBucketSize);
        this.minReadSize = 8192;
        this.maxReadSize = 8192;
        this.activeEnd = new BucketPointer(0, 0);
        this.dataSupplier = dataSupplier;
    }

    protected int nextBucketSize() {
        long activeSize = arrayOps.length(buckets[activeEnd.idx]);

        int maxBucketSize = Integer.MAX_VALUE / 2;
        int nextSize = Math.min(Ints.saturatedCast(activeSize * 2), maxBucketSize);
        return nextSize;

    }

    public int doRead(ArraySink<A> dst, Channel reader) { // ByteArrayChannel reader, ByteBuffer dst) {
        int result = 0;

        BucketPointer pointer = reader.pointer;
        if (pointer == null) {
            BucketPointer end = activeEnd;

            // Try to translate the logical linear position to a physical pointer
            pointer = getPointer(arrayOps, buckets, end, reader.pos);
            if (pointer == null) {
                if(isDataSupplierConsumed) {
                    return -1;
                } else {
                    long requestedPos = reader.pos;
                    loadDataUpTo(requestedPos);
                    end = activeEnd;
                    pointer = getPointer(arrayOps, buckets, end, reader.pos);

                    if(pointer == null) {
                        if(isDataSupplierConsumed) {
                            return -1;
                        } else {
                            throw new IllegalStateException("Should not happen: Could not map pointer position despite all data known");
                        }
                    }
                }
            }

            // Cache a valid pointer with the channel
            reader.pointer = pointer;
        }

        int bucketIdx = pointer.idx;
        int bucketPos = pointer.pos;

        for(;;) {
            int remainingDstLen = dst.remaining();
            if(remainingDstLen == 0) {
                if(result == 0) {
                    result = -1;
                }
                break;
            }

            A currentBucket = buckets[bucketIdx];

            // Copy the end marker to avoid race conditions when reading
            // its two attributes
            BucketPointer end = activeEnd;

            boolean isInLastBucket = bucketIdx == end.idx;
            int remainingBucketLen = isInLastBucket
                ? end.pos - bucketPos
                : arrayOps.length(currentBucket) - bucketPos
                ;

            if (remainingBucketLen == 0) {
                if (isInLastBucket) {
                    if(result != 0) {
                        // We have already read something on this iteration, return
                        break;
                    } else {
                        // We reached the bucket end and have not read anything so far
                        if(!isDataSupplierConsumed) {
                            synchronized(this) {
                                if (bucketPos == end.pos && bucketIdx == end.idx && !isDataSupplierConsumed) {
                                    loadData(dst.limit());
                                    continue;
                                }
                            }
                        } else {
                            result = -1;
                        }
                    }
                } else {
                    ++bucketIdx;
                    bucketPos = 0;
                    continue;
                }
            }

            int n = Math.min(remainingDstLen, remainingBucketLen);
            dst.put(currentBucket, bucketPos, n);
            result += n;
            pointer.pos = bucketPos += n;
            reader.pos += n;
            pointer.idx = bucketIdx;
            //pos += n;
        }

        return result;
    }

    /**
     * Preload data up to including the requested position.
     * It is inclusive in order to allow for checking whether the requested position is in range.
     *
     * @param requestedPos
     */
    protected void loadDataUpTo(long requestedPos) {
        while (!isDataSupplierConsumed && knownDataSize <= requestedPos) {
            synchronized(this) {
                if (!isDataSupplierConsumed && knownDataSize <= requestedPos) {
                    // System.out.println("load upto " + requestedPos);
                    int needed = Ints.saturatedCast(requestedPos - knownDataSize);
                    loadData(needed);
                }
            }
        }
    }

    /**
     * fetch a chunk from the input stream
     */
    protected void loadData(int needed) {
        if (!isDataSupplierConsumed) {
            ensureCapacityInActiveBucket();

            A activeBucket = buckets[activeEnd.idx];

            int len = Math.min(needed, maxReadSize);

            len = Math.max(len, minReadSize);
            len = Math.min(len, arrayOps.length(activeBucket) - activeEnd.pos);

            if(len != 0) {
                int n;
                try {
                    n = dataSupplier.read(activeBucket, activeEnd.pos, len);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if(n > 0) {
                    activeEnd.pos += n;
                    knownDataSize += n;
                } else if (n == -1) {
                    isDataSupplierConsumed = true;
                } else if (n == 0) {
                    throw new IllegalStateException("Data supplier returned 0 bytes");
                } else {
                    throw new IllegalStateException("Invalid return value: " + n);
                }
            }
        }
    }

    protected void ensureCapacityInActiveBucket() {
        A activeBucket = buckets[activeEnd.idx];
        int capacity = arrayOps.length(activeBucket) - activeEnd.pos;
        if (capacity == 0) {
            int nextBucketSize = nextBucketSize();
            if(nextBucketSize == 0) {
                throw new IllegalStateException("Bucket of size 0 generated");
            }

            int newEndIdx = activeEnd.idx + 1;
            if (newEndIdx >= buckets.length) {
                // Double number of buckets if possible
                int numNewBuckets = buckets.length * 2;
                if (numNewBuckets < buckets.length) {
                    numNewBuckets = Integer.MAX_VALUE;
                }

                A[] newBuckets = (A[])new Object[numNewBuckets];
                System.arraycopy(buckets, 0, newBuckets, 0, buckets.length);
                buckets = newBuckets;
            }

            // Allocate a new bucket
            // System.out.println("Allocating " + nextBucketSize);
            buckets[newEndIdx] = arrayOps.create(nextBucketSize);
            activeEnd = new BucketPointer(newEndIdx, 0);
        }
    }

    @Override
    public long getCapacity() {
        // TODO Make configurable
        return Long.MAX_VALUE;
    }

    /** Read a certain amount of data from a channel into this buffer starting at a certain position */
    public void write(long offsetInBuffer, ReadableChannel<A> source, int amount) throws IOException {
        BucketPointer pointer;
        if (offsetInBuffer == knownDataSize) {
            pointer = activeEnd;
        } else if (offsetInBuffer < knownDataSize) {
            pointer = getPointer(arrayOps, buckets, activeEnd, offsetInBuffer);
        } else {
            // TODO Allocate buckets as needed
            throw new UnsupportedOperationException("Appending data past the end not yet supported");
        }

        // int offset = arrOffset;
        int remainingInputLen = amount;
        for (;;) {
            if (pointer.idx == activeEnd.idx) {
                ensureCapacityInActiveBucket();
            }

            A bucket = buckets[pointer.idx];
            int remainingBucketLen = arrayOps.length(bucket) - pointer.pos;
            int readLen = Math.min(remainingInputLen, remainingBucketLen);
            int n = source.read(bucket, pointer.pos, readLen);
            if (n < 0) {
                break;
            }

            remainingInputLen -= n;
            if (remainingInputLen > 0) {
                ++pointer.idx;
                pointer.pos = 0;
            } else {
                break;
            }
        }
    }

    @Override
    public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
        write(offsetInBuffer, ReadableChannels.of(arrayOps, arrayWithItemsOfTypeT, arrOffset), arrLength);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        return newReadChannel(srcOffset).read(tgt, tgtOffset, length);
    }

    @Override
    public ReadableChannel<A> newReadChannel(long offset) {
        return new Channel(offset);
    }

    public class Channel
        extends ChannelBase
        implements ReadableChannel<A>
    {
        protected BucketPointer pointer;
        protected long pos;

        public Channel(long pos) {
            this(pos, null);
        }

        public Channel(long pos, BucketPointer pointer) {
            super();
            this.pointer = pointer;
            this.pos = pos;
        }

        @Override
        public ArrayOps<A> getArrayOps() {
            return arrayOps;
        }

        @Override
        public int read(A array, int position, int length) throws IOException {
            ensureOpen();
            ArraySink<A> sink = new ArraySink.ArraySinkArray<>(arrayOps, array, position, position + length);
            int result = doRead(sink, this);
            return result;
        }
    }

    public static BufferOverReadableChannel<byte[]> createForBytes() {
        ReadableChannel<byte[]> channel = ReadableChannels.wrap(new ByteArrayInputStream(new byte[0]));
        return new BufferOverReadableChannel<>(ArrayOps.BYTE, 4096, channel);
    }

    public static BufferOverReadableChannel<byte[]> createForBytes(InputStream in) {
        ReadableChannel<byte[]> channel = ReadableChannels.wrap(in);
        return new BufferOverReadableChannel<>(ArrayOps.BYTE, 4096, channel);
    }

    public static void main(String[] args) throws IOException {
        // BufferedInputStream bin = new BufferedInputStream(in);
        //bin.mark(Integer.MAX_VALUE);

        byte[] dst = new byte[4096];
        int n;
        long r = 0;

        for (int i = 0; i < 10; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            // bin.mark(Integer.MAX_VALUE);
            // InputStream channel = bin;
            ReadableChannel<byte[]> channel;

            InputStream in = new BoundedInputStream(Files.newInputStream(Path.of("/tmp/test.ttl")), Integer.MAX_VALUE - 1000);
            BufferOverReadableChannel<byte[]> bufferedChannel = null;
            if (false) {
                in = new BufferedInputStream(in);
                in.mark(Integer.MAX_VALUE);
                channel = ReadableChannels.wrap(in);
            } else {
                bufferedChannel = BufferOverReadableChannel.createForBytes(in);
                channel = bufferedChannel.newReadChannel();
            }

            // ReadableChannel<byte[]> channel = ReadableChannels.wrap(in);
            {
                Hasher hasher = Hashing.sha256().newHasher();
                ByteStreams.copy(ReadableChannels.newInputStream(channel), Funnels.asOutputStream(hasher));
                String hash = hasher.hash().toString();
                System.out.println("Hash: " + hash);
            }

            if (bufferedChannel != null) {
                Hasher hasher = Hashing.sha256().newHasher();
                Buffer<byte[]> memBuffer = bufferedChannel.getBuffer();
                channel = memBuffer.newReadChannel();
                ByteStreams.copy(ReadableChannels.newInputStream(channel), Funnels.asOutputStream(hasher));
                String hash = hasher.hash().toString();
                System.out.println("Hash2: " + hash);
            }

//                r = 0;
//                while ((n = channel.read(dst, 0, dst.length)) != -1) {
//                    r +=n;
//                }
            // bin.reset();
            System.out.println("Bytes read on interation " + i + " " + r + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f);
            channel.close();
        }
    }
}


