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
    public static <T, E, V, C extends Collection<V>> Accumulator<T, E, C> combineAccumulators(
            Accumulator<T, E, C> needle,
            Accumulator<T, E, C> haystack,
            UnaryOperator<Accumulator<T, E, C>> accumulatorCloner,
            Function<? super V, ? extends T> valueToItem,
            E env) {
        if (needle.getValue().size() > haystack.getValue().size()) {
            // Swap
            Accumulator<T, E, C> tmp = needle; needle = haystack; haystack = tmp;
        }

        Accumulator<T, E, C> result = accumulatorCloner.apply(haystack);
        for (V value : needle.getValue()) {
            T reductionItem = valueToItem.apply(value);
            result.accumulate(reductionItem, env);
        }

        return result;
    }

    /**
     * Create a serializable java8 collector from a parallel aggregator.
     *
     */
    public static <I, E, O, ACC extends Accumulator<I, E, O>> Collector<I, ?, O> createCollector(ParallelAggregator<I, E, O, ACC> agg, E env) {
        return new CollectorFromParallelAggregator<>(agg, env);
//        return SerializableCollectorImpl.create(
//                agg::createAccumulator,
//                // Accumulator::accumulate,
//                agg::combine,
//                Accumulator::getValue);
    }
}
