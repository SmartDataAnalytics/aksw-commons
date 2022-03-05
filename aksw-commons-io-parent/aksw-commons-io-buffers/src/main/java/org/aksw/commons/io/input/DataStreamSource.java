package org.aksw.commons.io.input;

import com.google.common.collect.Range;

public interface DataStreamSource<A> {
    // Offsets should start with 0
    DataStream<A> newDataStream(Range<Long> range);
}
