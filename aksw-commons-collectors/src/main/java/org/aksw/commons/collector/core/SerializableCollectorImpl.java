package org.aksw.commons.collector.core;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.lambda.serializable.SerializableBiConsumer;
import org.aksw.commons.lambda.serializable.SerializableBinaryOperator;
import org.aksw.commons.lambda.serializable.SerializableCollector;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;

/**
 * A collector implementation that implements Serializable.
 * Note that the constructor does NOT mandate the use of serializable versions of the lambdas.
 * In order to create an instance from lambdas that are made serializable 
 * use the method {@link #create(SerializableSupplier, SerializableBiConsumer, SerializableBinaryOperator, SerializableFunction)}
 * 
 * 
 * @author raven
 *
 * @param <T>
 * @param <A>
 * @param <R>
 */
public class SerializableCollectorImpl<T, A, R>
	implements SerializableCollector<T, A, R>
	// implements Collector<T, A, R>, Serializable
{
	private static final long serialVersionUID = 448920416560172402L;
	
	protected Supplier<A> supplier;
	protected BiConsumer<A, T> accumulator;
	protected BinaryOperator<A> combiner;
	protected Function<A, R> finisher;
	
	public SerializableCollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator,
			BinaryOperator<A> combiner, Function<A, R> finisher) {
		super();
		this.supplier = supplier;
		this.accumulator = accumulator;
		this.combiner = combiner;
		this.finisher = finisher;
	}

	@Override public BiConsumer<A, T> accumulator() { return accumulator; }

	// TODO Improve Characteristics
	@Override public Set<Characteristics> characteristics() { return Collections.emptySet(); }
	@Override public BinaryOperator<A> combiner() { return combiner; }
	@Override public Function<A, R> finisher() { return finisher; }
	@Override public Supplier<A> supplier() { return supplier; }
	
	public static <T, A, R> SerializableCollectorImpl<T, A, R> create(
			SerializableSupplier<A> supplier,
			SerializableBiConsumer<A, T> accumulator,
			SerializableBinaryOperator<A> combiner,
			SerializableFunction<A, R> finisher) {
		return new SerializableCollectorImpl<>(supplier, accumulator, combiner, finisher);
	}

};
