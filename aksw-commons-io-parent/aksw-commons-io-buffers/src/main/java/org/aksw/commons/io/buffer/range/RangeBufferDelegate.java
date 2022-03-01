package org.aksw.commons.io.buffer.range;

import org.aksw.commons.collection.rangeset.RangeSetDelegateBase;
import org.aksw.commons.io.buffer.array.BufferLikeDelegate;
import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.buffer.plain.BufferDelegateBase;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public interface RangeBufferDelegate<A>
    extends BufferLikeDelegate<A, RangeBuffer<A>>, RangeBuffer<A>
{

    @Override
    default RangeSet<Long> getCoveredRanges(Range<Long> localRange) {
        return new RangeSetDelegateBase<Long>() {
            @Override
            public RangeSet<Long> getDelegate() {
                return RangeBufferDelegate.this.getDelegate().getCoveredRanges(localRange);
            }
        };
    }

    @Override
    default RangeSet<Long> getRanges() {
        return new RangeSetDelegateBase<Long>() {
            @Override
            public RangeSet<Long> getDelegate() {
                return RangeBufferDelegate.this.getDelegate().getRanges();
            }
        };
        // return getDelegate().getRanges();
    }

    @Override
    default Long getOffsetInRanges() {
        return getDelegate().getOffsetInRanges();
    }

    @Override
    default RangeBuffer<A> slice(long offset, long length) {
        return new RangeBufferDelegateBase<A>() {
            @Override
            public RangeBuffer<A> getDelegate() {
                return RangeBufferDelegate.this.getDelegate().slice(offset, length);
            }
        };
        // return getDelegate().slice(offset, length);
    }

    @Override
    default Buffer<A> getBackingBuffer() {
        return new BufferDelegateBase<A>() {
            @Override
            public Buffer<A> getDelegate() {
                return RangeBufferDelegate.this.getBackingBuffer();
            }
        };
        // return getDelegate().getBackingBuffer();
    }

//	@Override
//	public ArrayOps<A> getArrayOps() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public long getCapacity() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength)
//			throws IOException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public RangeBuffer<A> getDelegate() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
