package org.aksw.commons.io.slice;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.ReadableChannelBase;


public class ReadableChannelOverSliceAccessor<A>
    extends ReadableChannelBase<A>
{
    protected SliceAccessor<A> accessor;
    protected long posInSlice;

    public ReadableChannelOverSliceAccessor(SliceAccessor<A> accessor, long posInSlice) {
        super();
        this.accessor = accessor;
        this.posInSlice = posInSlice;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return accessor.getSlice().getArrayOps();
    }

    @Override
    public void closeActual() throws IOException {
        accessor.close();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        accessor.claimByOffsetRange(posInSlice, posInSlice + length);
        int result = accessor.unsafeRead(array, position, posInSlice, length);
        if (result > 0) {
            posInSlice += result;
        }
        return result;
    }
}
