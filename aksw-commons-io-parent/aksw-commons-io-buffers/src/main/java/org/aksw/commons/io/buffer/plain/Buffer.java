package org.aksw.commons.io.buffer.plain;

import java.util.List;

import org.aksw.commons.io.buffer.array.BufferLike;

import com.google.common.math.LongMath;

public interface Buffer<A>
    extends BufferLike<A>
{
    /** Create a sub-buffer view of this buffer */
    default Buffer<A> slice(long offset, long length) {
        if (LongMath.checkedAdd(offset, length) > getCapacity()) {
            throw new RuntimeException("Sub-buffer extends over capacity of this buffer");
        }

        // TODO If this buffer is already a sub-buffer then prevent wrapping it again for performance

        return new SubBufferImpl<>(this, offset, length);
    }

    /**
     * Create a list over this buffer. The size of the list will be Ints.saturatedCast() of the buffer's capacity.
     * For this reason, it's recommended to use appropriately sliced buffers
     */
    default <T> List<T> asList() {
        return new ListOverBuffer<>(this);
    }
}
