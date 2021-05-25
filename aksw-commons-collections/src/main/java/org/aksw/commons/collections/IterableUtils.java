package org.aksw.commons.collections;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

public class IterableUtils {
    
    public static <T> T expectOneItem(Iterable<T> iterable) {
    	return IteratorUtils.expectOneItem(iterable.iterator());
    }

    public static <T> T expectZeroOrOneItems(Iterable<T> iterable) {
    	return IteratorUtils.expectZeroOrOneItems(iterable.iterator());
    }
	
    /**
     * Will only compare as many items as there are in the shorter iterable
     * 
     * @param itemComparator
     * @return
     */
    public static <T> Comparator<? super Iterable<? extends T>> newComparatorForIterablesOfEqualLength(Comparator<? super T> itemComparator) {
    	return (a, b) -> compareIterablesOfEqualLength(a, b, itemComparator);
    }

    /**
     * Will only compare as many items as there are in the shorter iterable
     * 
     * @param a
     * @param b
     * @param itemComparator
     * @return
     */
    public static <T> int compareIterablesOfEqualLength(
    		Iterable<? extends T> a,
    		Iterable<? extends T> b,
    		Comparator<? super T> itemComparator) {
    	int result = Streams.zip(Streams.stream(a), Streams.stream(b), (x, y) -> itemComparator.compare(x, y))
    		.mapToInt(x -> x)
    		.filter(x -> x != 0)
    		.findFirst().orElse(0);
    	
    	return result;
    }
    
    public static <T> int compareByLengthThenItems(Iterable<? extends T> a, Iterable<? extends T> b, Comparator<? super T> itemComparator) {
    	int result = ComparisonChain.start()
				.compare(Iterables.size(a), Iterables.size(b))
				.compare(a, b, newComparatorForIterablesOfEqualLength(itemComparator))
				.result();
		return result;
    }
}
