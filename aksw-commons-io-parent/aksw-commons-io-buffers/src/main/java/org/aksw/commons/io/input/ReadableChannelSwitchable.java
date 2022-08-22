package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/** Decorator meant to switch from a buffered stream to a non-buffering one */
public class ReadableChannelSwitchable<A>
    extends ReadableChannelDecoratorBase<A, ReadableChannel<A>>
{
    protected ReadWriteLock rwl = new ReentrantReadWriteLock();

    public ReadableChannelSwitchable(ReadableChannel<A> delegate) {
        super(delegate);
    }

    /** Acquires the write lock and sets the decoratee */
    public void setDecoratee(Supplier<ReadableChannel<A>> decoratee) {
        Lock writeLock = rwl.writeLock();
        try {
            writeLock.lock();
            this.decoratee = decoratee.get();
        } finally {
            writeLock.unlock();
        }
    }

    /** Only call while locked */
    public void setDecoratee(ReadableChannel<A> decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        Lock readLock = rwl.readLock();
        try {
            readLock.lock();
            return super.read(array, position, length);
        } finally {
            readLock.unlock();
        }
    }

    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }

    public ReadableChannel<A> getDecoratee() {
        return decoratee;
    }
}
