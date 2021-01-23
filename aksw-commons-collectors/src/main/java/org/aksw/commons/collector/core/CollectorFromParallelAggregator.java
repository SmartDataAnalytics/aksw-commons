package org.aksw.commons.collector.core;

import java.util.Collections;
import java.util.Set;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.serializable.lambda.SerializableBiConsumer;
import org.aksw.commons.serializable.lambda.SerializableBinaryOperator;
import org.aksw.commons.serializable.lambda.SerializableCollector;
import org.aksw.commons.serializable.lambda.SerializableFunction;
import org.aksw.commons.serializable.lambda.SerializableSupplier;

public class CollectorFromParallelAggregator<T, R, A extends Accumulator<T, R>>
	implements SerializableCollector<T, A, R>
{
	private static final long serialVersionUID = 1L;

	protected ParallelAggregator<T, R, A> aggregator;
	
	public CollectorFromParallelAggregator(ParallelAggregator<T, R, A> aggregator) {
		super();
		this.aggregator = aggregator;
	}
	
	public ParallelAggregator<T, R, A> getAggregator() {
		return aggregator;
	}

	@Override
	public SerializableSupplier<A> supplier() {
		return aggregator::createAccumulator;
	}

	@Override
	public SerializableBiConsumer<A, T> accumulator() {
		return Accumulator::accumulate;
	}
	
	@Override
	public SerializableBinaryOperator<A> combiner() {
		return aggregator::combine;
	}

	@Override
	public SerializableFunction<A, R> finisher() {
		return Accumulator::getValue; 
	}

	// TODO Improve characteristics
	@Override
	public Set<Characteristics> characteristics() {
		return Collections.emptySet();
	}

}
