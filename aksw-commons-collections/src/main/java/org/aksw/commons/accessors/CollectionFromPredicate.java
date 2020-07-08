package org.aksw.commons.accessors;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import org.aksw.commons.collections.SinglePrefetchIterator;

import com.google.common.collect.Iterators;

public class CollectionFromPredicate<T, C extends Collection<T>>
    extends AbstractCollection<T>
{
    // FilteredIterator
    public static class IteratorFromPredicate<T, I extends Iterator<T>>
        extends SinglePrefetchIterator<T>
    {
        protected I baseIt;
        protected Predicate<? super T> predicate;

        public IteratorFromPredicate(I baseIt, Predicate<? super T> predicate) {
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


    protected C backend;
    protected Predicate<Object> predicate;

    public CollectionFromPredicate(C backend, Predicate<Object> predicate) {
        super();
        this.backend = backend;
        this.predicate = predicate;
    }


    @Override
    public boolean add(T e) {
        if(!predicate.test(e)) {
            throw new IllegalArgumentException("add failed because item was rejected by predicate " + e);
        }
        boolean result = backend.add(e);
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean accepted = predicate.test(o);

        boolean result = accepted
                ? backend.contains(o)
                : false;

        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean accepted = predicate.test(o);

        boolean result = accepted
                ? backend.remove(o)
                : false;

        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> baseIt = backend.iterator();

        return new IteratorFromPredicate<>(baseIt, predicate);
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }
}


//  return new SinglePrefetchIterator<F>() {
//@Override
//protected F prefetch() throws Exception {
//  while(baseIt.hasNext()) {
//      B b = baseIt.next();
//      F f;
//      try {
//          f = converter.convert(b);
//      } catch(Exception e) {
//          /* Ignore items that fail to convert */
//          continue;
//      }
//      return f;
//  }
//  return finish();
//}
//@Override
//public void doRemove(F item) { baseIt.remove(); }
//};
//
