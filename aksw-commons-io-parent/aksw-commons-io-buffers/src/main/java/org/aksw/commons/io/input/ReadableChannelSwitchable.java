package org.aksw.commons.io.input;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/** Decorator meant to switch from a buffered stream to a non-buffering one */
public class ReadableChannelSwitchable<A>
    extends ReadableChannelLocking<A, ReadableChannel<A>>
{
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

    /** Only call while locked. Take care to properly close the prior delegate!  */
    public void setDecoratee(ReadableChannel<A> decoratee) {
        this.decoratee = decoratee;
    }

    public ReadableChannel<A> getDecoratee() {
        return decoratee;
    }
}
