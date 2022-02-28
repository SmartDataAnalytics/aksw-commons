package org.aksw.commons.io;

import com.google.common.collect.Range;

public interface SequentialReaderSource<A> {
	// Offsets should start with 0
	SequentialReader<A> newInputStream(Range<Long> range);
}
