package org.aksw.commons.collection.slice;

import java.io.IOException;

import org.aksw.commons.buffer.plain.Buffer;
import org.aksw.commons.util.array.ArrayOps;

import com.google.common.collect.Range;

public class SliceInMemory<A>
    extends SliceBase<A>
{
    // protected RangeBuffer<A> rangeBuffer;
    protected SliceMetaDataBasic metaData;
    protected Buffer<A> buffer;

    protected SliceInMemory(ArrayOps<A> arrayOps, Buffer<A> buffer) {
        super();
        this.metaData = new SliceMetaDataImpl();
        this.arrayOps = arrayOps;
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
        };
    }

    @Override
    public void sync() throws IOException {
    }
}
