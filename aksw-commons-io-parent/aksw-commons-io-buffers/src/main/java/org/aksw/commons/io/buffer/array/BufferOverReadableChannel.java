package org.aksw.commons.io.buffer.array;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.buffer.plain.SubBuffer;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;
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
     * Return the data supplier. The data supplier usually needs to be closed
     * when it is no longer needed.
     */
    public ReadableChannel<A> getDataSupplier() {
        return dataSupplier;
    }

    /** Set a new data supplier whose data can be appended to this buffer on demand */
    public void setDataSupplier(ReadableChannel<A> dataSupplier) {
        this.dataSupplier = dataSupplier;
        this.isDataSupplierConsumed = false;
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
        public BucketPointer(int bucketIdx, int itemIdx) {
            super();
            this.bucketIdx = bucketIdx;
            this.itemIdx = itemIdx;
        }

        int bucketIdx;
        int itemIdx;
        @Override
        public String toString() {
            return "BucketPointer [buckedIdx=" + bucketIdx + ", itemIdx=" + itemIdx + "]";
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

        int eidx = end.bucketIdx;
        int epos = end.itemIdx;
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

        BucketPointer result = i == end.bucketIdx && tmp > epos
                ? null
                : new BucketPointer(i, Ints.checkedCast(tmp));
        return result;
    }

    public BufferOverReadableChannel(
            ArrayOps<A> arrayOps,
            int initialBucketSize,
            ReadableChannel<A> dataSupplier,
            int minReadSize) {
        if (initialBucketSize <= 0) {
            throw new IllegalArgumentException("Bucket size must not be 0");
        }

        this.arrayOps = arrayOps;
        this.buckets = (A[])new Object[8];
        buckets[0] = arrayOps.create(initialBucketSize);
        this.minReadSize = minReadSize;
        this.maxReadSize = minReadSize;
        this.activeEnd = new BucketPointer(0, 0);
        this.dataSupplier = dataSupplier;
    }

    protected int nextBucketSize() {
        long activeSize = arrayOps.length(buckets[activeEnd.bucketIdx]);

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

        int bucketIdx = pointer.bucketIdx;
        int bucketPos = pointer.itemIdx;

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

            boolean isInLastBucket = bucketIdx == end.bucketIdx;
            int remainingBucketLen = isInLastBucket
                ? end.itemIdx - bucketPos
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
                                if (bucketPos == end.itemIdx && bucketIdx == end.bucketIdx && !isDataSupplierConsumed) {
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
            pointer.itemIdx = bucketPos += n;
            reader.pos += n;
            pointer.bucketIdx = bucketIdx;
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

            A activeBucket = buckets[activeEnd.bucketIdx];

            int len = Math.min(needed, maxReadSize);

            len = Math.max(len, minReadSize);
            len = Math.min(len, arrayOps.length(activeBucket) - activeEnd.itemIdx);

            if(len != 0) {
                int n;
                try {
                    n = dataSupplier.read(activeBucket, activeEnd.itemIdx, len);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if(n > 0) {
                    activeEnd.itemIdx += n;
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
        A activeBucket = buckets[activeEnd.bucketIdx];
        int capacity = arrayOps.length(activeBucket) - activeEnd.itemIdx;
        if (capacity == 0) {
            int nextBucketSize = nextBucketSize();
            if(nextBucketSize == 0) {
                throw new IllegalStateException("Bucket of size 0 generated");
            }

            int newEndIdx = activeEnd.bucketIdx + 1;
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
            if (pointer.bucketIdx == activeEnd.bucketIdx) {
                ensureCapacityInActiveBucket();
            }

            A bucket = buckets[pointer.bucketIdx];
            int remainingBucketLen = arrayOps.length(bucket) - pointer.itemIdx;
            int readLen = Math.min(remainingInputLen, remainingBucketLen);
            int n = source.read(bucket, pointer.itemIdx, readLen);
            if (n < 0) {
                break;
            }

            remainingInputLen -= n;
            if (remainingInputLen > 0) {
                ++pointer.bucketIdx;
                pointer.itemIdx = 0;
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
        int result;
        // FIXME If the jvm's assert switch is enabled (-ea) then
        // the channel's leak detection makes things awfully slow
        try (SeekableReadableChannel<A> channel = new Channel(false, srcOffset, null)) {
            result = channel.read(tgt, tgtOffset, length);
        }
        return result;
    }

    @Override
    public SeekableReadableChannel<A> newReadableChannel() throws IOException {
        return new Channel(0l);
    }

    public class Channel
        extends ChannelBase
        implements SeekableReadableChannel<A>
    {
        protected BucketPointer pointer;
        protected long pos;

        public Channel(long pos) {
            this(pos, null);
        }

        public Channel(long pos, BucketPointer pointer) {
            this(true, pos, pointer);
        }

        public Channel(boolean enableInitializationStackTrace, long pos, BucketPointer pointer) {
            super(enableInitializationStackTrace);
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

        @Override
        public long position() {
            return pos;
        }

        @Override
        public void position(long pos) {
            this.pos = pos;

            // TODO For small differences in pos we should adjust the pos relatively to the old position
            this.pointer = null;
        }

        @Override
        public SeekableReadableChannel<A> cloneObject() { // throws CloneNotSupportedException {
            return new Channel(pos, pointer);
        }
    }

    public static BufferOverReadableChannel<byte[]> createForBytes() {
        return createForBytes(InputStream.nullInputStream());
    }

    public static BufferOverReadableChannel<byte[]> createForBytes(InputStream in) {
        return createForBytes(in, 8192);
    }

    public static BufferOverReadableChannel<byte[]> createForBytes(InputStream in, int minReadSize) {
        ReadableChannel<byte[]> channel = ReadableChannels.wrap(in);
        return new BufferOverReadableChannel<>(ArrayOps.BYTE, 8, channel, minReadSize);
    }


    public static void main(String[] args) throws IOException {
        Random random = new Random();
        Path path = Path.of("/tmp/test.ttl");
        try (FileChannel fc = FileChannel.open(path);
            InputStream in = Files.newInputStream(path)) {
            BufferOverReadableChannel<byte[]> buffer = BufferOverReadableChannel.createForBytes(in);

            long size = fc.size();

            System.out.println("Size:" + size);
            for (int i = 0; i < 50000; ++i) {

                long pos = Math.abs(random.nextLong()) % size;
                pos %= Integer.MAX_VALUE;

                System.out.println(String.format("Iteration %d, pos %d", i, pos));

                fc.position(pos);
                ByteBuffer bb = ByteBuffer.allocate(1);
                fc.read(bb);
                byte expected = bb.get(0);
                byte actual;

                try (SeekableReadableChannel<byte[]> bc = buffer.newReadableChannel()) {
                    byte[] b = new byte[1];
                    bc.position(pos);
                    bc.read(b, 0, 1);
                    bc.position(pos);
                    //actual = b[0];

                    try (SeekableReadableChannel<byte[]> bc2 = bc.cloneObject()) {
                        // bc.position(pos);
                        //bc.read(b, 0, 1);
                        // actual = b[0];
                        CharSequence cs = ReadableChannels.asCharSequence(bc2, Ints.saturatedCast(size));
                        actual = (byte)cs.charAt(Ints.checkedCast(pos));
                    }

                }

                if (expected != actual) {
                    throw new RuntimeException("Results differ at position " + pos);
                }
                //Assert.assertEquals(expected, actual);
            }
        }
    }

    public static void main2(String[] args) throws IOException {
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
                channel = bufferedChannel.newReadableChannel();
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
                channel = memBuffer.newReadableChannel();
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


