package org.aksw.commons.collector.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import org.aksw.commons.collector.core.AggInputFilter.AccInputFilter;
import org.aksw.commons.collector.core.AggInputSplit.AccInputSplit;
import org.aksw.commons.collector.core.AggInputTransform.AccInputTransform;
import org.aksw.commons.collector.core.AggOutputTransform.AccOutputTransform;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableBiConsumer;
import org.aksw.commons.lambda.serializable.SerializableBiFunction;
import org.aksw.commons.lambda.serializable.SerializableBinaryOperator;
import org.aksw.commons.lambda.serializable.SerializableCollector;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializablePredicate;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.lambda.serializable.SerializableToDoubleFunction;
import org.aksw.commons.lambda.serializable.SerializableToIntFunction;
import org.aksw.commons.lambda.serializable.SerializableToLongFunction;
import org.aksw.commons.util.serializable.SerializableDoubleSummaryStatistics;
import org.aksw.commons.util.serializable.SerializableIntSummaryStatistics;
import org.aksw.commons.util.serializable.SerializableLongSummaryStatistics;



/**
 * Builder for parallel aggregators.
 *
 * Static 'from' methods start the builder chain.
 * All methods that perform modifications return a new independent builder object.
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


    /** Pass on input to sub-acc if the predicate evaluates to true*/
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

    public static <I, K, J, O,
        ACC extends Accumulator<J, O>,
        AGG extends ParallelAggregator<J, O, ACC>> AggInputSplit<I, K, J, O, ACC, AGG>
    inputSplit(
            Set<K> fixedKeys,
            boolean considerNewKeys,
            SerializableFunction<? super I, ? extends Set<? extends K>> keyMapper,
            SerializableBiFunction<? super I, ? super K, ? extends J> valueMapper,
            AGG state) {
        return new AggInputSplit<>(state, fixedKeys, considerNewKeys, keyMapper, valueMapper);
    }

    /** Simple input split: Partition input by a key derived from it */
    public static <I, K, O,
        ACC extends Accumulator<I, O>,
        AGG extends ParallelAggregator<I, O, ACC>> AggInputSplit<I, K, I, O, ACC, AGG>
    inputSplit(
            SerializableFunction<? super I, ? extends K> keyMapper, AGG state) {
        return new AggInputSplit<>(state, in -> Collections.singleton(keyMapper.apply(in)), (in, key) -> in);
    }



//	public static <I, O, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> AggInputFilter<I, O, ACC, AGG>
//	inputFilter(SerializablePredicate<? super I> inputFilter, AGG state) {
//	 return new AggInputFilter<>(state, inputFilter);
//	}

    public static <I, J, O, ACC extends Accumulator<J, O>, AGG extends ParallelAggregator<J, O, ACC>> AggInputTransform<I, J, O, ACC, AGG>
        inputTransform(SerializableFunction<? super I, ? extends J> inputTransform, AGG state) {
        return new AggInputTransform<>(state, inputTransform);
    }

    public static <I, J, O, ACC extends Accumulator<J, O>, AGG extends ParallelAggregator<J, O, ACC>> AggInputFlatMap<I, J, O, ACC, AGG>
        inputFlatMap(SerializableFunction<I, Iterator<J>> inputTransform, AGG state) {
        return new AggInputFlatMap<>(state, inputTransform);
    }

    public static <I, O, P, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> AggOutputTransform<I, O, P, ACC, AGG>
        outputTransform(AGG state, SerializableFunction<? super O, ? extends P> outputTransform) {
        return new AggOutputTransform<>(state, outputTransform);
    }

    public static <T>
        ParallelAggregator<T, Set<T>, Accumulator<T, Set<T>>> hashSetSupplier()
    {
        return collectionSupplier(HashSet<T>::new);
    }

    public static <T>
    ParallelAggregator<T, List<T>, Accumulator<T, List<T>>> arrayListSupplier()
    {
        return collectionSupplier(ArrayList<T>::new);
    }


    public static <T>
        ParallelAggregator<T, Set<T>, Accumulator<T, Set<T>>> setSupplier(SerializableSupplier<? extends Set<T>> setSupplier)
    {
        return collectionSupplier(setSupplier);
    }

    /** We provide the SetOverMap collection type which transparently gives access to the underlying map
     * This way we can reuse the collection machinery for accumulation and still type-safely extract the map eventually */
    public static <K, V>
        ParallelAggregator<Entry<K, V>, SetOverMap<K, V>, Accumulator<Entry<K, V>, SetOverMap<K, V>>> mapSupplier(SerializableSupplier<? extends Map<K, V>> mapSupplier)
    {
        return naturalAccumulator(() -> new AccCollection<>(new SetOverMap<K, V>(mapSupplier.get())));
    }

    public static <T, C extends Collection<T>>
        ParallelAggregator<T, C, Accumulator<T, C>> collectionSupplier(SerializableSupplier<? extends C> colSupplier)
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

    /** An aggregator that broadcasts its input to two sub-aggregators that accept the same input. */
    public static <I, O1, O2>
    ParallelAggregator<I, Entry<O1, O2>, ?> inputBroadcast(
            ParallelAggregator<I, O1, ?> agg1,
            ParallelAggregator<I, O2, ?> agg2)
    {
        return new AggInputBroadcast<>(agg1, agg2);
    }

    /** An aggregator that broadcasts its input to multiple sub-aggregators that accept the same input */
    public static <I, K, O>
    AggInputBroadcastMap<I, K, O> inputBroadcastMap(
            Map<K, ParallelAggregator<I, O, ?>> subAggMap)
    {
        return new AggInputBroadcastMap<>(subAggMap);
    }


    public static <I>
    ParallelAggregator<I, I, Accumulator<I, I>> fold(
            SerializableSupplier<I> zeroElementSupplier,
            SerializableBinaryOperator<I> plusOperator)
    {
        return new AggReduce<>(zeroElementSupplier, plusOperator);
    }


    public static <T, R, A> AggFromCollector<T, R, A> fromCollector(
            SerializableSupplier<A> supplier,
            SerializableBiConsumer<A, T> accumulator,
            SerializableBinaryOperator<A> combiner,
            SerializableFunction<A, R> finisher) {
        return fromCollector(supplier, accumulator, combiner, finisher, Collections.emptySet());
    }

    public static <T, R, A> AggFromCollector<T, R, A> fromCollector(
            SerializableSupplier<A> supplier,
            SerializableBiConsumer<A, T> accumulator,
            SerializableBinaryOperator<A> combiner,
            SerializableFunction<A, R> finisher,
            Set<Collector.Characteristics> characteristics
            ) {

        SerializableCollector<T, A, R> collector = SerializableCollectorImpl.create(supplier, accumulator, combiner, finisher, characteristics);
        return fromCollector(collector);
    }


    public static <T, A, R> AggFromCollector<T, R, A> fromCollector(SerializableCollector<T, A, R> collector) {
        return new AggFromCollector<>(collector);
    }


    public static
    ParallelAggregator<Long, Long, Accumulator<Long, Long>> maxLong()
    {
        return fold(() -> 0l, Math::max);
    }

    public static
    ParallelAggregator<Integer, Integer, Accumulator<Integer, Integer>> maxInteger()
    {
        return fold(() -> 0, Math::max);
    }


    static final Set<Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));


    public static <T> AggFromCollector<T, SerializableIntSummaryStatistics, SerializableIntSummaryStatistics>
        summarizingInt(SerializableToIntFunction<T> mapper)
    {
        return fromCollector(
                SerializableIntSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsInt(t)),
                (l, r) -> { l.combine(r); return l; },
                x -> x,
                CH_ID);
    }

    public static <T> AggFromCollector<T, SerializableLongSummaryStatistics, SerializableLongSummaryStatistics>
        summarizingLong(SerializableToLongFunction<T> mapper)
    {
        return fromCollector(
                SerializableLongSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsLong(t)),
                (l, r) -> { l.combine(r); return l; },
                x -> x,
                CH_ID);
    }

    public static <T> AggFromCollector<T, SerializableDoubleSummaryStatistics, SerializableDoubleSummaryStatistics>
        summarizingDouble(SerializableToDoubleFunction<T> mapper)
    {
        return fromCollector(
                SerializableDoubleSummaryStatistics::new,
                (r, t) -> r.accept(mapper.applyAsDouble(t)),
                (l, r) -> { l.combine(r); return l; },
                x -> x,
                CH_ID);
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


