package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


/**
 * Aggregator whose accumulators apply a binary to their
 * value and their input to compute their new value.
 * Accumulators are initialized with a zero element.
 * 
 * Used as a basis for: min, max, sum (which in turn are the basis for e.g. avg)
 */
public class AggBinaryOperator<I>
	implements ParallelAggregator<I, I, Accumulator<I, I>>, Serializable
{
	private static final long serialVersionUID = 0L;

	protected Supplier<I> zeroElementSupplier;
	protected BinaryOperator<I> plusOperator;
	
	public AggBinaryOperator(Supplier<I> zeroElementSupplier, BinaryOperator<I> plusOperator) {
		super();
		this.zeroElementSupplier = zeroElementSupplier;
		this.plusOperator = plusOperator;
	}

	@Override
	public Accumulator<I, I> createAccumulator() {
		I zeroElement = zeroElementSupplier.get();
		return new AccBinaryOperatorImpl(zeroElement);
	}

	@Override
	public Accumulator<I, I> combine(Accumulator<I, I> a, Accumulator<I, I> b) {
		I va = a.getValue();
		I vb = b.getValue();
		I combinedValue = plusOperator.apply(va, vb);
		
		return new AccBinaryOperatorImpl(combinedValue);
	}

	public class AccBinaryOperatorImpl
		implements Accumulator<I, I>, Serializable
	{
		private static final long serialVersionUID = 0;
	
		protected I value;
		
		public AccBinaryOperatorImpl(I value) {
			super();
			this.value = value;
		}
	
		@Override
		public void accumulate(I input) {
			value = plusOperator.apply(value, input);
		}
	
		@Override
		public I getValue() {
			return value;
		}
	}
}

