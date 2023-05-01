package org.aksw.commons.collections;


import java.io.Closeable;
import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:39:36 PM
 */
public abstract class PrefetchIterator<T>
    implements Iterator<T>, Closeable
{
    //private static Logger logger = Logger.getLogger(PrefetchIterator.class);
    private Iterator<T>	current		= null;
    private boolean		finished	= false;

    abstract protected Iterator<T> prefetch()
        throws Exception;

    protected PrefetchIterator()
    {
    }

    private void preparePrefetch()
    {
        if (finished) {
            return;
        }

        current = null;
        Throwable x = null;
        try {
            // Prefetch may return empty iterators - skip them.
            do {
                current = prefetch();
            } while(current != null && !current.hasNext());
        } catch(Throwable e) {
            current = null;
            x = e;
        }
        if (current == null) {
            close();
            finished = true;

            if (x != null) {
                throw new RuntimeException(x);
            }
        }
    }

    private Iterator<T> getCurrent()
    {
        if (current == null || !current.hasNext()) {
            preparePrefetch();
        }

        return current;
    }

    public boolean hasNext()
    {
        return getCurrent() != null;
    }

    public T next()
    {
        return getCurrent().next();
    }

    @Override
    public void close() {

    }

    public void remove()
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}