package org.aksw.commons.collections.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class is intended for cases where the primary selector draws with replacement.
 * Once the primary selector is consumed, the failover is consulted.
 * 
 * @author Claus Stadler, Oct 12, 2018
 *
 * @param <T>
 */
public class WeigthedSelectorFailover<T>
	implements WeightedSelector<T>
{
	protected WeightedSelector<T> primary;
	protected WeightedSelector<T> failover;
	
	public WeigthedSelectorFailover<T> clone() {
		return new WeigthedSelectorFailover<T>(primary.clone(), failover.clone());
	}
	
	public WeigthedSelectorFailover(WeightedSelector<T> primary, WeightedSelector<T> failover) {
		super();
		this.primary = primary;
		this.failover = failover;
	}

	@Override
	public Entry<T, ? extends Number> sampleEntry(Number t) {
		WeightedSelector<T> delegate = primary.isEmpty() ? failover : primary;
		return delegate.sampleEntry(t);
	}

	@Override
	public Collection<Entry<T, ? extends Number>> entries() {
		List<Entry<T, ? extends Number>> result = new ArrayList<>();
		result.addAll(primary.entries());
		result.addAll(failover.entries());
		return result;
	}
	
	@Override
	public boolean isEmpty() {
		boolean result = primary.isEmpty() && failover.isEmpty();
		return result;
	}
}
