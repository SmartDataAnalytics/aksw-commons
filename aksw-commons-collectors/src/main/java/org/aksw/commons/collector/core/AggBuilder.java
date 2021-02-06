package org.aksw.commons.collector.core;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collector.core.AggInputFilter.AccInputFilter;
import org.aksw.commons.collector.core.AggInputSplit.AccInputSplit;
import org.aksw.commons.collector.core.AggInputTransform.AccInputTransform;
import org.aksw.commons.collector.core.AggOutputTransform.AccOutputTransform;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableBiFunction;
import org.aksw.commons.lambda.serializable.SerializableBinaryOperator;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializablePredicate;
import org.aksw.commons.lambda.serializable.SerializableSupplier;

/**
 * Builder for parallel aggregators.
 * 
 * Static 'from' methods start the builder chain.
 * All methods that performa modifications return a new independent builder object.
 * Because type expressions can become complex there are three getters that return the
 * wrapped aggregator either fully typed, as a parallel aggregator or as a simple aggregator:
 * {@link #getFullyTyped()}, {@link #getAsParallelAggregator()}, {@link #getAsAggregator()}. 
 * 
 * 
 * @author raven
 *
 * @param <I> The current aggregator's input type
 * @param <O>  The current aggregator's output type
 * @param <ACC> The current aggregator's accumulator type
 * @param <AGG> The current aggregator's own type
 */
public class AggBuilder<I, O, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> {
	
	/*
	 * Static constructors - allow for for a more natural read order (outer-to-inner)
	 * but not for fluent-style chaining.
	 */
	
	
	public static <I, O, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> AggInputFilter<I, O, ACC, AGG>
		inputFilter(SerializablePredicate<? super I> inputFilter, AGG state) {
		 return new AggInputFilter<>(state, inputFilter);
	}

	
	/** InputSplit: Create the <b>same</b> accumulator type for each split of the input */
	public static <I, K, J, O,
		ACC extends Accumulator<J, O>,
		AGG extends ParallelAggregator<J, O, ACC>> AggInputSplit<I, K, J, O, ACC, AGG>
	inputSplit(
			SerializableFunction<? super I, ? extends Set<? extends K>> keyMapper,
			SerializableBiFunction<? super I, ? super K, ? extends J> valueMapper,
			AGG state) {
		return new AggInputSplit<>(state, keyMapper, valueMapper);
	}

//	public static <I, O, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> AggInputFilter<I, O, ACC, AGG>
//	inputFilter(SerializablePredicate<? super I> inputFilter, AGG state) {
//	 return new AggInputFilter<>(state, inputFilter);
//	}
	
	public static <I, J, O, ACC extends Accumulator<J, O>, AGG extends ParallelAggregator<J, O, ACC>> AggInputTransform<I, J, O, ACC, AGG>
		inputTransform(SerializableFunction<? super I, ? extends J> inputTransform, AGG state) {
		return new AggInputTransform<>(state, inputTransform);
	}

	

	public static <I, O, P, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> AggOutputTransform<I, O, P, ACC, AGG>
		outputTransform(AGG state, SerializableFunction<? super O, ? extends P> outputTransform) {
		return new AggOutputTransform<>(state, outputTransform);
	}

	public static <T, C extends Collection<T>>
		ParallelAggregator<T, C, Accumulator<T, C>> collectionSupplier(SerializableSupplier<C> colSupplier)
	{
		return naturalAccumulator(() -> new AccCollection<>(colSupplier.get()));
	}

	public static <T, C extends Collection<T>>
	ParallelAggregator<T, C, Accumulator<T, C>> naturalAccumulator(SerializableSupplier<? extends Accumulator<T, C>> accSupplier)
	{
		return new AggNatural<>(accSupplier);
	}

	
	public static <I>
	ParallelAggregator<I, Long, Accumulator<I, Long>> counting()
	{
		return new AggCounting<I>();
	}

	public static <I, O1, O2>
	ParallelAggregator<I, Entry<O1, O2>, ?> inputBroadcast(
			ParallelAggregator<I, O1, ?> agg1,
			ParallelAggregator<I, O2, ?> agg2)
	{
		return new AggInputBroadcast<>(agg1, agg2);
	}

	public static <I, K, O>
	AggInputBroadcastMap<I, K, O> inputBroadcastMap(
			Map<K, ParallelAggregator<I, O, ?>> subAggMap)
	{
		return new AggInputBroadcastMap<>(subAggMap);
	}

	
	public static <I>
	ParallelAggregator<I, I, Accumulator<I, I>> binaryOperator(
			SerializableSupplier<I> zeroElementSupplier,
			SerializableBinaryOperator<I> plusOperator)
	{
		return new AggBinaryOperator<>(zeroElementSupplier, plusOperator);
	}

	public static
	ParallelAggregator<Long, Long, Accumulator<Long, Long>> maxLong()
	{
		return binaryOperator(() -> 0l, Math::max);
	}

	public static
	ParallelAggregator<Integer, Integer, Accumulator<Integer, Integer>> maxInteger()
	{
		return binaryOperator(() -> 0, Math::max);
	}

	
	
	/*
	 * Fluent chaining - likely to be deprecated because the static constructors allow for a
	 * top-down construction which feels much more natural than the bottom-up by the fluent style.
	 * 
	 */

	protected AGG state;
	
	public AggBuilder(AGG state) {
		super();
		this.state = state;
	}

	public AGG getFullyTyped() {
		return state;
	}

	public ParallelAggregator<I, O, ?> getAsParallelAggregator() {
		return state;
	}

	public Aggregator<I, O> getAsAggregator() {
		return state;
	}

	
	public static <I, O,
					ACC extends Accumulator<I, O>,
					AGG extends ParallelAggregator<I, O, ACC>>
		AggBuilder<I, O, ACC, AGG> from(AGG agg)
	{
		return new AggBuilder<>(agg);
	}

	public static <T, C extends Collection<T>>
	AggBuilder<T, C, Accumulator<T, C>, ParallelAggregator<T, C, Accumulator<T, C>>> fromNaturalAccumulator(SerializableSupplier<? extends Accumulator<T, C>> accSupplier)
	{
		return from(new AggNatural<>(accSupplier));
	}

	public static <T, C extends Collection<T>>
	AggBuilder<T, C, Accumulator<T, C>, ParallelAggregator<T, C, Accumulator<T, C>>> fromCollectionSupplier(SerializableSupplier<C> colSupplier)
	{
		return fromNaturalAccumulator(() -> new AccCollection<>(colSupplier.get()));
	}

	public AggBuilder<I, O, AccInputFilter<I, O, ACC>, AggInputFilter<I, O, ACC, AGG>> withInputFilter(SerializablePredicate<? super I> inputFilter) {
		 return from(new AggInputFilter<>(state, inputFilter));
	}

	public <H> AggBuilder<H, O, AccInputTransform<H, I, O, ACC>, AggInputTransform<H, I, O, ACC, AGG>>
		withInputTransform(SerializableFunction<? super H, ? extends I> inputTransform) {
		return from(new AggInputTransform<>(state, inputTransform));
	}

	public <H, K> AggBuilder<H, Map<K, O>, AccInputSplit<H, K, I, O, ACC>, AggInputSplit<H, K, I, O, ACC, AGG>> withInputSplit(
			SerializableFunction<? super H, ? extends Set<? extends K>> keyMapper,
			SerializableBiFunction<? super H, ? super K, ? extends I> valueMapper			
	) {
		return from(new AggInputSplit<>(state, keyMapper, valueMapper));
	}
	
	/**
	 * Supply a function O -&gt; P in order to turn an Aggregator&lt;I, O&gt; into Aggregator&lt;I, P&gt; 
	 * 
	 * @param <P>
	 * @param outputTransform
	 * @return
	 */
	public <P> AggBuilder<I, P, AccOutputTransform<I, O, P, ACC>, AggOutputTransform<I, O, P, ACC, AGG>>
		withOutputTransform(SerializableFunction<? super O, ? extends P> outputTransform)
	{
		return from(new AggOutputTransform<>(state, outputTransform));
	}

}


