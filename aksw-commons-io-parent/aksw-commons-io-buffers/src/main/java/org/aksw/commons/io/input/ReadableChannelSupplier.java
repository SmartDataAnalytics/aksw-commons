package org.aksw.commons.io.input;

public interface ReadableChannelSupplier<A> {
    ReadableChannel<A> newChannel();
}
