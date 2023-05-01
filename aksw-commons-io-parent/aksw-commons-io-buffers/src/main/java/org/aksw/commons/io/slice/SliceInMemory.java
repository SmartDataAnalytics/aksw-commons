package org.aksw.commons.io.slice;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.util.closeable.AutoCloseableBase;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.closeable.Disposable;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * Slice implementation backed only by a single {@link Buffer}.
 * This implementation never evicts data hence using it with large
 * amounts of data is subject to out of memory issues.
 *
 * @author raven
 *
 * @param <A>
 */
public class SliceInMemory<A>
    extends SliceBase<A>
{
    // protected RangeBuffer<A> rangeBuffer;
    protected SliceMetaDataBasic metaData;
    protected Buffer<A> buffer;

    protected SliceInMemory(ArrayOps<A> arrayOps, Buffer<A> buffer) {
        super(arrayOps);
        this.metaData = new SliceMetaDataImpl();
        this.buffer = buffer;
    }

    public static <A> SliceInMemory<A> create(ArrayOps<A> arrayOps, Buffer<A> buffer) {
        return new SliceInMemory<>(arrayOps, buffer);
    }

    @Override
    protected SliceMetaDataBasic getMetaData() {
        return metaData;
    }


    @Override
    public SliceAccessor<A> newSliceAccessor() {
        return new SliceAccessor<A>() {

            @Override
            public Slice<A> getSlice() {
                return SliceInMemory.this;
            }

            @Override
            public void claimByOffsetRange(long startOffset, long endOffset) {
            }

            @Override
            public void lock() {
            }

            @Override
            public void write(long offset, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
                buffer.write(offset, arrayWithItemsOfTypeT, arrOffset, arrLength);
                getMetaData().getLoadedRanges().add(Range.closedOpen(offset, offset + arrLength));
            }

            @Override
            public int unsafeRead(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
                return buffer.readInto(tgt, tgtOffset, srcOffset, length);
            }

            @Override
            public void unlock() {
            }

            @Override
            public void releaseAll() {
            }

            @Override
            public void close() {
            }

            /** There is no eviction so nothing to do */
            @Override
            public void addEvictionGuard(RangeSet<Long> ranges) {

            }
        };
    }

    @Override
    public void sync() throws IOException {
    }

    @Override
    public Disposable addEvictionGuard(RangeSet<Long> range) {
        return new AutoCloseableWithLeakDetectionBase()::close;
    }
}
