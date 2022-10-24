package org.aksw.commons.io.input;

public class SeekableReadableChannels {
    /** Wrap a readable channel such the positioning via skipping becomes possible */
    public static <A> SeekableReadableChannel<A> wrapForwardSeekable(ReadableChannel<A> channel, long basePos) {
        return new SeekableReadableChannelOverReadableChannel<>(channel, basePos);
    }
}
