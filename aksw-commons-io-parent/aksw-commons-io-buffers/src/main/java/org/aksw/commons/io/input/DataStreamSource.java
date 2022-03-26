package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.HasArrayOps;

import com.google.common.collect.Range;

public interface DataStreamSource<A>
    extends HasArrayOps<A>
{
    /** Offsets typically start with 0 but the interface contract leaves that unspecified */
    DataStream<A> newDataStream(Range<Long> range) throws IOException;

    /** The size; -1 if unknown */
    long size() throws IOException;
}
