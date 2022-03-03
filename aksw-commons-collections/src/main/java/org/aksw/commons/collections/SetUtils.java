package org.aksw.commons.collections;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ForwardingSet;

public class SetUtils {
//    public static <T> Set<T> asSet(Iterable<T> c) {
//        return (c instanceof Set) ? (Set<T>) c : Sets.newLinkedHashSet(c);
//    }

	/**
	 * If the argument is already an instance of a set then cast it to a set and return it;
	 * otherwise create a new LinkedHashSet initialized with the given items
	 */
    public static <T> Set<T> asSet(Iterable<T> c)
    {
        return (c instanceof Set) ? (Set<T>)c : CollectionUtils.newCollection(LinkedHashSet::new, c);
    }

    public static <T> Set<T> newLinkedHashSet(Iterator<? extends T> c)
    {
    	return CollectionUtils.newCollection(LinkedHashSet::new, c);
    }


    /**
     * Short hand for
     * Set<T> result = source.stream().map(fn).collect(Collectors.toSet())
     * 
     * Maps a set of keys to a corresponding set of values via a given map
     * TODO Probably this method can be replaced by something from guava
     *
     * @param set
     * @param map
     * @return
     */
    public static <K, V> Set<V> mapSet(Set<K> set, Map<K, V> map) {
        Set<V> result = new HashSet<V>();
        for(K item : set) {
            V v = map.get(item);
            result.add(v);
        }

        return result;
    }

    public static <T> Set<T> newForwardingSet(Supplier<? extends Set<T>> setSupplier) {
    	return new ForwardingSet<T>() {
			@Override
			protected Set<T> delegate() {
				return setSupplier.get();
			}
		};
    }

}

