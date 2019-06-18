package org.aksw.commons.collections.selector;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public interface WeightedSelector<T>
	extends Cloneable
{
//	@Override
	/**
	 * Cloning mechanism to allow simple implementation of backtracking.
	 * 
	 * @return
	 */
	WeightedSelector<T> clone();

	Entry<T, ? extends Number> sampleEntry(Number t);
	
	// Note: Collection is allowed to contain duplicates
	Collection<Entry<T, ? extends Number>> entries();
	

	default T sample(Number t) {
		Entry<T, ? extends Number> e = sampleEntry(t);
		T result = e != null ? e.getKey() : null;
		return result;
	}

	// Derived map that sums up the weights (as doubles) of each item
	default Map<T, ? extends Number> entryMap() {
		Map<T, Double> result = entries().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().doubleValue(), (a, b) -> a.doubleValue() + b.doubleValue()));
		return result;
	}
	
	default boolean isEmpty() {
		Collection<Entry<T, ? extends Number>> tmp = entries();
		boolean result = tmp.isEmpty();
		return result;
	}
}
