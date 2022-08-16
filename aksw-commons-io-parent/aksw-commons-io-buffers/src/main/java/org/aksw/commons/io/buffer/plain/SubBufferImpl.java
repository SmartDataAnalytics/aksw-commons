package org.aksw.commons.io.buffer.plain;

public class SubBufferImpl<A>
    implements SubBuffer<A>
{
    protected Buffer<A> backend;
    protected long start;
    protected long length;

    public SubBufferImpl(Buffer<A> backend, long start, long length) {
        super();
        this.backend = backend;
        this.start = start;
        this.length = length;
    }

    @Override
    public Buffer<A> getBackend() {
        return backend;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getLength() {
        return length;
    }


//	@Override
//	public Buffer<A> slice(long offset, long length) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void put(long offset, Object item) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public Object get(long index) {
//		throw new UnsupportedOperationException();
//	}
}

