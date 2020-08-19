package org.aksw.commons.collections;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringIterator<T, I extends Iterator<T>>
    extends SinglePrefetchIterator<T>
{
    protected I baseIt;
    protected Predicate<? super T> predicate;

    public FilteringIterator(I baseIt, Predicate<? super T> predicate) {
        super();
        this.baseIt = baseIt;
        this.predicate = predicate;
    }

    protected T prefetch() throws Exception {
        while(baseIt.hasNext()) {
            T cand = baseIt.next();
            boolean accepted = predicate.test(cand);
            if(!accepted) {
                continue;
            }

            return cand;
        }
        return finish();
    }

    @Override
    protected void doRemove(T item) {
        baseIt.remove();
    }
}