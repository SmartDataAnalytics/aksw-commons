package org.aksw.commons.collections.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.Maps;

public abstract class WeightedSelectorMutableBase<T>
	implements WeightedSelector<T>
{
	protected List<Entry<T, ? extends Number>> entries;
	protected double nextOffset;
	
	public abstract WeightedSelectorMutableBase<T> clone();

	public static double eps(double d) {
		double e = 0.00001;
		return d >= 0.0 && d < e ? e : d;
	}
	
	public WeightedSelectorMutableBase() {
		this(new ArrayList<>());
	}
	
	public WeightedSelectorMutableBase(List<Entry<T, ? extends Number>> entries) {
		this.entries = entries;
		this.nextOffset = entries.stream().map(Entry::getValue).mapToDouble(Number::doubleValue).sum();
	}
	
	public Entry<Integer, Entry<T, ? extends Number>> sampleEntryWithIndex(Number t) {
		double d = Objects.requireNonNull(t).doubleValue();
		if(d < 0.0 || d > 1.0) {
			throw new IllegalArgumentException("Argument must be in the interval [0, 1]");
		}
		
		double key = d * nextOffset;
		
		double current = 0.0;
		Entry<T, ? extends Number> match = null;
		
		int i = -1;
		for(Entry<T, ? extends Number> e : entries) {
			// The result is the entry whose offset weight is smaller yet closest to key  
			match = e;
			++i;
	
			double next = current + eps(e.getValue().doubleValue());
			if(key < next) {
				break;
			}
			current = next;
		}
	
		Entry<Integer, Entry<T, ? extends Number>> result = match == null
				? null : Maps.immutableEntry(i, match);
		
		return result;
	}
	
	@Override
	public Entry<T, ? extends Number> sampleEntry(Number t) {
		Entry<Integer, Entry<T, ? extends Number>> e = sampleEntryWithIndex(t);
		Entry<T, ? extends Number> result = e == null ? null : e.getValue();
		return result;
	}
	
	@Override
	public Collection<Entry<T, ? extends Number>> entries() {
		return entries;
	}
	
	public void put(T item, Number weight) {
		put(Maps.immutableEntry(item, weight));
	}
	
	public void put(Entry<T, ? extends Number> e) {
		entries.add(e);
		nextOffset += eps(e.getValue().doubleValue());
	}
	
	public void putAll(Collection<? extends Entry<T, ? extends Number>> items) {
		for(Entry<T, ? extends Number> e : items) {
			put(e);
		}
	}
	
	public boolean remove(Entry<T, Double> e) {
		boolean result = entries.removeIf(item -> Objects.equals(item, e));
		return result;
	}
}