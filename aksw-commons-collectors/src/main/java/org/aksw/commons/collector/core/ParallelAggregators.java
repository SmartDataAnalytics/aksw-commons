package org.aksw.commons.collector.core;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

public class ParallelAggregators {

	/**
	 * Merge two accumulators.
	 * 
	 * @param <T>
	 * @param <C>
	 * @param needle
	 * @param haystack
	 * @param accumulatorCloner The cloner; may return its argument for in place changes.
	 * @return
	 */
	public static <T, V, C extends Collection<V>> Accumulator<T, C> combineAccumulators(
			Accumulator<T, C> needle,
			Accumulator<T, C> haystack,
			UnaryOperator<Accumulator<T, C>> accumulatorCloner,
			Function<? super V, ? extends T> valueToItem) {
		if (needle.getValue().size() > haystack.getValue().size()) {
			// Swap
			Accumulator<T, C> tmp = needle; needle = haystack; haystack = tmp;
		}
		
		Accumulator<T, C> result = accumulatorCloner.apply(haystack);
		for (V value : needle.getValue()) {
			T reductionItem = valueToItem.apply(value);
			result.accumulate(reductionItem);
		}
		
		return result;
	}
	
	/**
	 * Create a serializable java8 collector from a parallel aggregator.
	 * 
	 */
	public static <I, O, ACC extends Accumulator<I,O>> Collector<I, ?, O> createCollector(ParallelAggregator<I, O, ACC> agg) {
		return SerializableCollectorImpl.create(
				agg::createAccumulator,
				Accumulator::accumulate, 
				agg::combine,
				Accumulator::getValue);
	}
}
