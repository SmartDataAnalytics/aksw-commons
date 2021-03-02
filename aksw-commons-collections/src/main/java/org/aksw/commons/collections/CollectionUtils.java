package org.aksw.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * User: raven
 * Date: 4/17/11
 * Time: 12:36 AM
 */
public class CollectionUtils {

	/**
	 * Create a new collection and initialize it with items from an iterator
	 * Mainly useful in cases where use of guava could cause issues (e.g. hadoop/spark)
	 */
	public static <T, C extends Collection<T>> C newCollection(Supplier<C> collectionSupplier, Iterator<? extends T> itemIt) {
		C result = collectionSupplier.get();
		while (itemIt.hasNext() ) {
			T item = itemIt.next();
			result.add(item);
		}
		return result;
	}

	/**
	 * Create a new collection and initialize it with items from an iterable
	 * Mainly useful in cases where use of guava could cause issues (e.g. hadoop/spark)
	 */
	public static <T, C extends Collection<T>> C newCollection(Supplier<C> collectionSupplier, Iterable<? extends T> items) {
		return newCollection(collectionSupplier, items.iterator());
	}

	
    /**
     * Given an iterable A whose elements are iterables, this method will return the first
     * element of A.
     * If no such element exist, an empty iterable is returned rather than null.
     *
     *
     * @param iterable
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T extends Iterable<S>> Iterable<S> safeGetFirst(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();

        return it.hasNext() ? it.next() : Collections.<S>emptySet();
    }

    public static <T> List<List<T>> chunk(Iterable<T> col, int batchSize)
    {
        List<List<T>> result = new ArrayList<List<T>>();

        List<T> chunk = new ArrayList<T>();

        Iterator<T> it = col.iterator();
        while(it.hasNext()) {
            chunk.add(it.next());

            if(chunk.size() >= batchSize || !it.hasNext()) {
                result.add(chunk);

                if(it.hasNext())
                    chunk = new ArrayList<T>();
            }
        }

        return result;
    }

    public static <T> Set<T> asSet(Iterable<T> c)
    {
        return (c instanceof Set) ? (Set<T>)c : newCollection(LinkedHashSet::new, c);
    }

    /** Transforms an array into a Hashset. @author Konrad Höffner */
    static <T> Set<T> asSet(T[] a)
    {
        Set<T> s = new HashSet<T>();
        for(T e:a) {s.add(e);}
        return s;
    }
}