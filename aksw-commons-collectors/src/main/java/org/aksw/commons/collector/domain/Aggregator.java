package org.aksw.commons.collector.domain;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import org.aksw.commons.collector.core.AggFinish;
import org.aksw.commons.lambda.serializable.SerializableFunction;

/**
*
* @author raven
*
*
* @param <B> The type of bindings being accumulated by the accumulator
* @param <T> The result object of the accumulation
*/
@FunctionalInterface
public interface Aggregator<B, T> {
   Accumulator<B, T> createAccumulator();

   
   /** Transform the final value of an aggregation */
   default <U> Aggregator<B, U> finish(SerializableFunction<T, U> transform) {
	   return AggFinish.create(this, transform);
   }
   
   /**
    * Convenience function to sequentially accumulate a given stream. Closes the stream when done.
    * For {@link ParallelAggregator} use stream.collect(parallelAggregator.asCollector());
    */
   default Optional<T> accumulateAll(Stream<? extends B> stream) {
	   try (Stream<? extends B> tmp = stream) {
		   return accumulateAll(stream.iterator());
	   }
   }

   /** Convenience function to sequentially accumulate a given iterable */
   default Optional<T> accumulateAll(Iterable<? extends B> iterable) {
	   return accumulateAll(iterable.iterator());
   }

   /** Convenience function to sequentially accumulate a given iterator */
   default Optional<T> accumulateAll(Iterator<? extends B> it) {
	   Accumulator<B, T> acc = createAccumulator();
	   while (it.hasNext()) {
		   B item = it.next();
		   acc.accumulate(item);
	   }
	   return Optional.ofNullable(acc.getValue());
   }

}