package org.aksw.commons.collector.domain;

import java.util.stream.Collector;

import org.aksw.commons.collector.core.CollectorFromParallelAggregator;

/**
 * Aggregator suitable for parallel processing.
 * Extends {@link Aggregator} with a method to combine accumulators.
 *
 * @author raven
 *
 */
public interface ParallelAggregator<I, E, O, ACC extends Accumulator<I, E, O>>
     extends Aggregator<I, E, O>
{
    @Override
    ACC createAccumulator();

    /**
     * Combine accumulators. This method is allowed to mutate any of if its arguments,
     * hence accumulators should be considered exhausted after combination.
     *
     * @param a First participant of combination
     * @param b Second participant of combination
     * @return Combined accumulator
     */
    ACC combine(ACC a, ACC b);


    // O getValueEmpty();

    /**
     * Combine method that works on Objects. Exact type signatures are typically
     * too unwieldy to be carried around in user code.
     * Delegates to {@link #combine(Accumulator, Accumulator)} thus its notes apply.
     *
     * @param x First participant of combination
     * @param y Second participant of combination
     * @return Combined accumulator
     */
    default ACC combineRaw(Object x, Object y) {
        @SuppressWarnings("unchecked")
        ACC a = (ACC)x;
        @SuppressWarnings("unchecked")
        ACC b = (ACC)y;
        ACC result = combine(a, b);
        return result;
    }


    default Collector<I, ?, O> asCollector() {
        return asCollector(null);
    }

    /**
     * Wrap this aggregator as a Java8 collector.
     *
     * @return The collector view of this aggregator
     */
    default Collector<I, ?, O> asCollector(E env) {
        return new CollectorFromParallelAggregator<>(this, env);
    }
}
