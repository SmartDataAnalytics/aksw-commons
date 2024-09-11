package org.aksw.commons.io.input;

public interface ReadableChannelBuilder<A, X extends ReadableChannel<A>, B extends ReadableChannelBuilder<A, X, B>> {
    B setStart(long start);
    B setEnd(long end);
    B setAdvertiseBlocks(boolean offOrOn);
    X build();
}
