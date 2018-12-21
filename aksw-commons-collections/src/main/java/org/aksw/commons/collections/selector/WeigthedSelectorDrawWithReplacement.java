package org.aksw.commons.collections.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Weighted selector with draw with replacement semantics.
 * This means, any entry returned by sampleEntry
 * is removed from the candidate pool
 * and thus will no longer be returned by future invocations of these methods.
 * Note, that the convenience method sample is based on sampleEntry.
 * 
 * @author raven
 *
 * @param <T>
 */
public class WeigthedSelectorDrawWithReplacement<T>
	extends WeightedSelectorMutable<T>
{
	public WeigthedSelectorDrawWithReplacement() {
		super();
	}

	public WeigthedSelectorDrawWithReplacement(List<Entry<T, ? extends Number>> entries) {
		super(entries);
	}

	public WeightedSelectorMutable<T> clone() {
		return new WeigthedSelectorDrawWithReplacement<T>(new ArrayList<>(entries));
	}

	@Override
	public Entry<T, ? extends Number> sampleEntry(Number t) {
		Entry<Integer, Entry<T, ? extends Number>> e = sampleEntryWithIndex(t);

		Entry<T, ? extends Number> result = null;
		if(e != null) {
			int index = e.getKey();
			entries.remove(index);
			result = e.getValue();

			nextOffset -= result.getValue().doubleValue();
		}

		return result;
	}

	public static <T> WeigthedSelectorDrawWithReplacement<T> create(Map<T, ? extends Number> map) {
		return create(map.entrySet());
	}
	
	public static <T> WeigthedSelectorDrawWithReplacement<T> create(Collection<? extends Entry<T, ? extends Number>> entries) {
		return new WeigthedSelectorDrawWithReplacement<>(new ArrayList<>(entries));
	}
	
	//
//	public static <T> WeightedSelectorMutable<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
//		return create(items, Functions.identity(), getWeight);
//	}
//
////	public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
////		List<Entry<T, Double>> es = Streams.stream(items)
////				.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
////				collect(Collectors.toCollection(ArrayList::new));
////		
////		return new WeightedSelectorMutable<>(es);
////	}
//
//	public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
//		return configure((Function<List<Entry<T, Double>>, WeightedSelectorMutable<T>>)WeightedSelectorMutable::new, items, getEntity, getWeight);
//	}
//	
//	public static <X, T, S> S configure(Function<List<Entry<T, Double>>, S> ctor, Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
//		List<Entry<T, Double>> es = Streams.stream(items)
//				.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
//				collect(Collectors.toCollection(ArrayList::new));
//		
//		return ctor.apply(es);
//	}

}
