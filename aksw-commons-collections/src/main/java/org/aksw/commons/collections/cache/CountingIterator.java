package org.aksw.commons.collections.cache;

import java.util.Iterator;

public class CountingIterator<T>
    extends IteratorDecorator<T>
{
    protected long numItems;

    public CountingIterator(Iterator<T> delegate) {
        this(delegate, 0l);
    }

    public CountingIterator(Iterator<T> delegate, long numItems) {
        super(delegate);
        this.numItems = numItems;//new AtomicLong(numItems);
    }

    public long getNumItems() {
        return numItems; //.get();
    }

    @Override
    public T next() {
        T result = super.next();
        ++numItems;
        //numItems.incrementAndGet();
        return result;
    }
}
