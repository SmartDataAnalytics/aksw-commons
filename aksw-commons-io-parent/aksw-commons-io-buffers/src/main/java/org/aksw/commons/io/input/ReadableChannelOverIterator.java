package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.collect.Streams;

public class ReadableChannelOverIterator<T>
    extends ReadableChannelBase<T[]>
{
    protected ArrayOps<T[]> arrayOps;
    protected Iterator<T> iterator = null;
    protected Runnable closeAction;

    public ReadableChannelOverIterator(ArrayOps<T[]> arrayOps, Iterator<T> it, Runnable closeAction) {
        super();
        this.arrayOps = arrayOps;
        this.iterator = it;
        this.closeAction = closeAction;
    }


    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    /** Returns this channel as a stream - closing the stream closes this channel */
    public Stream<T> toStream() {
        return Streams.stream(iterator).onClose(this::close);
    }

    public Iterator<T> getIterator() {
        return iterator;
    }


    @Override
    public void closeActual() throws IOException {
        if (closeAction != null) {
            closeAction.run();
        }
    }

    @Override
    public int read(T[] array, int position, int length) throws IOException {
        ensureOpen();

        int i;
        for (i = 0; i < length && iterator.hasNext(); ++i) {
            Object value = iterator.next();
            arrayOps.set(array, position + i, value);
        }

        // return 0 if length is 0
        int result = i == 0 && length > 0 ? -1 : i;
        return result;
    }

    @Override
    public ArrayOps<T[]> getArrayOps() {
        return arrayOps;
    }
}
