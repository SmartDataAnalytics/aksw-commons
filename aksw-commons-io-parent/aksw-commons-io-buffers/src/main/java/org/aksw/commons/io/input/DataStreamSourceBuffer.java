package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.slice.Slice;

import com.google.common.collect.Range;

public class DataStreamSourceBuffer<A>
    implements DataStreamSource<A>
{
    protected DataStreamSource<A> delegate;
    protected Slice<A> buffer;

    @Override
    public ArrayOps<A> getArrayOps() {
        return delegate.getArrayOps();
    }

    @Override
    public DataStream<A> newDataStream(Range<Long> range) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long size() throws IOException {
        long result = delegate.size();
        return result;
    }


    class DataStreamBuffer
        extends DataStreamBase<A> {

        @Override
        public ArrayOps<A> getArrayOps() {
            return delegate.getArrayOps();
        }

        @Override
        public int read(A array, int position, int length) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }
    }
}
