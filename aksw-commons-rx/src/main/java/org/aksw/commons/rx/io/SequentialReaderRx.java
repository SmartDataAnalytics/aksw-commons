package org.aksw.commons.rx.io;

import java.io.IOException;
import java.util.Iterator;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.DataStream;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SequentialReaderRx<A>
    extends AutoCloseableWithLeakDetectionBase
    implements DataStream<A>
{
    protected Flowable<?> flowable;
    protected ArrayOps<A> arrayOps;

    protected Iterator<?> iterator = null;
    protected Disposable disposable;

    public SequentialReaderRx(Flowable<?> flowable, ArrayOps<A> arrayOps) {
        super();
        this.flowable = flowable;
        this.arrayOps = arrayOps;
    }

    public void start() {
        if (iterator == null) {
            synchronized (this) {
                if (iterator == null) {
                    ensureOpen();
                    iterator = flowable.blockingIterable().iterator();
                    disposable = (Disposable)iterator;
                }
            }
        }
    }

    public boolean isStarted() {
        return iterator != null;
    }

    @Override
    public void closeActual() throws IOException {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        ensureOpen();

        if (!isStarted()) {
            start();
        }

        int i;
        for (i = 0; i < length && iterator.hasNext(); ++i) {
            Object value = iterator.next();
            arrayOps.set(array, position + i, value);
        }

        return i == 0 ? -1 : i;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public boolean isOpen() {
        return !isClosed;
    }
}