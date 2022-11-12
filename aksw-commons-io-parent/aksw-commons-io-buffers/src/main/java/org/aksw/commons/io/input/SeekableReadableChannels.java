package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Objects;

public class SeekableReadableChannels {
    /** Wrap a readable channel such the positioning via skipping becomes possible */
    public static <A> SeekableReadableChannel<A> wrapForwardSeekable(ReadableChannel<A> channel, long basePos) {
        return new SeekableReadableChannelOverReadableChannel<>(channel, basePos);
    }
    
    public static <T> SeekableReadableChannel<T> closeShield(SeekableReadableChannel<T> in) {
        Objects.requireNonNull(in);
        return new SeekableReadableChannelDecoratorBase<>(in) {
        	@Override
        	public void close() throws IOException {
        		// No op / close shield
        	}
        };
    }
}
