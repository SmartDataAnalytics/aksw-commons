package org.aksw.commons.collector.core;

import java.util.Collections;
import java.util.Set;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableBiConsumer;
import org.aksw.commons.lambda.serializable.SerializableBinaryOperator;
import org.aksw.commons.lambda.serializable.SerializableCollector;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggregator == null) ? 0 : aggregator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollectorFromParallelAggregator<?, ?, ?> other = (CollectorFromParallelAggregator<?, ?, ?>) obj;
		if (aggregator == null) {
			if (other.aggregator != null)
				return false;
		} else if (!aggregator.equals(other.aggregator))
			return false;
		return true;
	}
}
