package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggInputBroadcastMap.AccInputBroadcastMap;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * An aggregator that broadcasts its input to multiple sub-aggregators that accept the same input
 * and have the same structure of the output
 * 
 * Serves the purpose to perform two independent type safe aggregations on the same input in a single pass.
 * 
 * @author raven
 *
 * @param <I> Input type
 * @param <K> The key type by which aggregators are referred to
 * @param <O> output type common to all aggregators
 */
public class AggInputBroadcastMap<I, K, O>
	implements ParallelAggregator<I, Map<K, O>, AccInputBroadcastMap<I, K, O>>, Serializable
{
	private static final long serialVersionUID = 0;


	public static interface AccInputBroadcastMap<I, K, O>
	extends Accumulator<I, Map<K, O>> {
		Map<K, Accumulator<I, O>> getSubAccMap();
	}
	
	protected Map<K, ParallelAggregator<I, O, ?>> subAggMap;
	
	public AggInputBroadcastMap(Map<K, ParallelAggregator<I, O, ?>> subAggMap) {
		super();
		this.subAggMap = subAggMap;
	}
	
	@Override
	public AccInputBroadcastMap<I, K, O> createAccumulator() {
		Map<K, Accumulator<I, O>> subAccMap = subAggMap.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().createAccumulator()));
		
		return new AccInputBroadcastMapImpl(subAccMap);
	}
	
	@Override
	public AccInputBroadcastMap<I, K, O> combine(
			AccInputBroadcastMap<I, K, O> a,
			AccInputBroadcastMap<I, K, O> b) {

		Map<K, Accumulator<I, O>> am = a.getSubAccMap();
		Map<K, Accumulator<I, O>> bm = b.getSubAccMap();
		
		Map<K, Accumulator<I, O>> newMap = subAggMap.entrySet().stream()
			.collect(Collectors.toMap(Entry::getKey, e -> {
				K key = e.getKey();
				ParallelAggregator<I, O, ?> subAgg = e.getValue();
				Accumulator<I, O> as = am.get(key);
				Accumulator<I, O> bs = bm.get(key);
				Accumulator<I, O> r = subAgg.combineRaw(as, bs);
				return r;
			}));
		
		return new AccInputBroadcastMapImpl(newMap); 
	}


	public class AccInputBroadcastMapImpl
		implements AccInputBroadcastMap<I, K, O>, Serializable
	{
		private static final long serialVersionUID = 0;
		
		protected Map<K, Accumulator<I, O>> keyToSubAcc;
		
		
		
		public AccInputBroadcastMapImpl(Map<K, Accumulator<I, O>> keyToSubAcc) {
			super();
			this.keyToSubAcc = keyToSubAcc;
		}
	
		@Override
		public void accumulate(I input) {
			for (Entry<K, ParallelAggregator<I, O, ?>> e : subAggMap.entrySet()) {
				Accumulator<I, O> acc = keyToSubAcc.computeIfAbsent(e.getKey(), k -> e.getValue().createAccumulator());
				acc.accumulate(input);
			}			
		}
	
		@Override
		public Map<K, O> getValue() {
			Map<K, O> result = keyToSubAcc.entrySet().stream()
					.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue()));
					
			return result;
		}
	
		@Override
		public Map<K, Accumulator<I, O>> getSubAccMap() {
			return keyToSubAcc;
		}
		
	}
}
