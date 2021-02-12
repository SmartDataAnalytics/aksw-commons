package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.stream.Collector;

import org.aksw.commons.collector.core.AggFromCollector.AccFromCollector;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * An aggregator from a collector.
 * Together with SerializableCollectorImpl a bridge is formed to
 * construct parallelizable aggregators from custom lambdas;
 * 
 * 
 * 
 */
public class AggFromCollector<I, O, A>
	implements ParallelAggregator<I, O, AccFromCollector<I, O, A>>, Serializable
{
	private static final long serialVersionUID = 0L;

	public interface AccFromCollector<I, O, A> extends Accumulator<I, O> {
		A getAccumulator();
	}
	
	protected Collector<I, A, O> collector;

	public AggFromCollector(Collector<I, A, O> collector) {
		super();
		this.collector = collector;
	}

	@Override
	public AccFromCollector<I, O, A> createAccumulator() {
		return new AccFromCollectorImpl(collector.supplier().get());
	}

	@Override
	public AccFromCollector<I, O, A> combine(AccFromCollector<I, O, A> a, AccFromCollector<I, O, A> b) {
		return new AccFromCollectorImpl(collector.combiner().apply(a.getAccumulator(), b.getAccumulator()));
	}
	
	public class AccFromCollectorImpl
		implements AccFromCollector<I, O, A>, Serializable {

		private static final long serialVersionUID = 1L;

		protected A accumulator;
		
		public AccFromCollectorImpl(A accumulator) {
			super();
			this.accumulator = accumulator;
		}

		@Override
		public A getAccumulator() {
			return accumulator;
		}

		@Override
		public void accumulate(I input) {
			collector.accumulator().accept(accumulator, input);
		}

		@Override
		public O getValue() {
			return collector.finisher().apply(accumulator);
		}
	}
}
