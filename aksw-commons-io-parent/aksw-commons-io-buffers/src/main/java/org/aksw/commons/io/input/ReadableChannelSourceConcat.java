package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.List;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

import com.google.common.collect.Range;


public class ReadableChannelSourceConcat<A>
    implements ReadableChannelSource<A>
{
    protected List<ReadableChannelSource<A>> members;
    protected ArrayOps<A> arrayOps;

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public ReadableChannel<A> newReadableChannel(Range<Long> range) throws IOException {
        return new DataStreamConcat(range);
    }

    @Override
    public long size() throws IOException {
        long result = 0;
        for (ReadableChannelSource<A> member : members) {
            long contrib = member.size();
            if (contrib < 0) {
                throw new IllegalStateException("Encountered member with unknown size in concat data stream source");
            }

            result += contrib;
        }
        return result;
    }

    class DataStreamConcat
        extends AutoCloseableWithLeakDetectionBase
        implements ReadableChannel<A>
    {
        protected Range<Long> range;
        protected ReadableChannel<A> current;
        protected long currentOffset;
        protected long currentExpectedSize;

        public DataStreamConcat(Range<Long> range) {
            super();
            this.range = range;
        }

        @Override
        public ArrayOps<A> getArrayOps() {
            return arrayOps;
        }

        @Override
        public int read(A array, int position, int length) throws IOException {


            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isOpen() {
            return !isClosed;
        }

    }

}

