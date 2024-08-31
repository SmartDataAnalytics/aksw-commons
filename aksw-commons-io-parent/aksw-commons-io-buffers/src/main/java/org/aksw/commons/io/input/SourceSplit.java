package org.aksw.commons.io.input;

public class SourceSplit<A, T extends ReadableChannelSource<A>> {
    protected ReadableChannelSource<A> source;
    protected long start;
    protected long end;

    public SourceSplit(T source, long start, long end) {
        super();
        this.source = source;
        this.start = start;
        this.end = end;
    }

    public ReadableChannelSource<A> getSource() {
        return source;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

//    public ReadableChannel<A> newChannel() throws IOException {
//        return source.newReadableChannel(start);
//    }

    @Override
    public String toString() {
        long len = end - start;
        return source + ":" + start + "+" + len;
    }
}
