package org.aksw.commons.collections.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;


public class WeightedSelectorMutable<T>
	extends WeightedSelectorMutableBase<T>
{
	public WeightedSelectorMutable<T> clone() {
		return new WeightedSelectorMutable<T>(new ArrayList<>(entries));
	}

	public WeightedSelectorMutable() {
		super();
	}

	public WeightedSelectorMutable(List<Entry<T, ? extends Number>> entries) {
		super(entries);
	}
	
	public static <T> WeightedSelectorMutable<T> create(Map<T, ? extends Number> map) {
		return create(map.entrySet(), Entry::getKey, Entry::getValue);
	}
	
	public static <T> WeightedSelectorMutable<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		return create(items, Functions.identity(), getWeight);
	}
	
	//public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
	//	List<Entry<T, Double>> es = Streams.stream(items)
	//			.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
	//			collect(Collectors.toCollection(ArrayList::new));
	//	
	//	return new WeightedSelectorMutable<>(es);
	//}
	
	public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		Function<List<Entry<T, ? extends Number>>, WeightedSelectorMutable<T>> ctor = WeightedSelectorMutable<T>::new;
		return configure(ctor, items, getEntity, getWeight);
	}
	
	public static <X, T, S> S configure(Function<List<Entry<T, ? extends Number>>, S> ctor, Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		List<Entry<T, ? extends Number>> es = Streams.stream(items)
				.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
				collect(Collectors.toCollection(ArrayList::new));
		
//		Consumer<List<? extends Entry<T, ? extends Number>>> c = xxx -> {};
//		c.accept(es);
		
		return ctor.apply(es);
	}
}
