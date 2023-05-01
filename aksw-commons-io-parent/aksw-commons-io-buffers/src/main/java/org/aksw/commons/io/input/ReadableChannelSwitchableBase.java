package org.aksw.commons.io.input;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class ReadableChannelSwitchableBase<A, X extends ReadableChannel<A>>
    extends ReadableChannelLocking<A, X>
{
    public ReadableChannelSwitchableBase(X delegate) {
        super(delegate);
    }

    /** Acquires the write lock and sets the decoratee */
    public void setDecoratee(Supplier<X> decoratee) {
        Lock writeLock = rwl.writeLock();
        try {
            writeLock.lock();
            this.decoratee = decoratee.get();
        } finally {
            writeLock.unlock();
        }
    }

    /** Only call while locked. Take care to properly close the prior delegate!  */
    public void setDecoratee(X decoratee) {
        this.decoratee = decoratee;
    }

    public X getDecoratee() {
        return decoratee;
    }
}
