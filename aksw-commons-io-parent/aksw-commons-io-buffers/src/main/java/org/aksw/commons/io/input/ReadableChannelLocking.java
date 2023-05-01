package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadableChannelLocking<A, X extends ReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
{
    protected ReadWriteLock rwl = new ReentrantReadWriteLock();

    public ReadableChannelLocking(X delegate) {
        super(delegate);
    }

    /** Override if any actions are needed; make sure to eventually call base.read()  */
    @Override
    public int read(A array, int position, int length) throws IOException {
        int result;
        Lock readLock = rwl.readLock();
        try {
            readLock.lock();
            result = super.read(array, position, length);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }
}
