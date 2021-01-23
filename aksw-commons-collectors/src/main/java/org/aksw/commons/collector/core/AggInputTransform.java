package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.Function;

import org.aksw.commons.collector.core.AggInputTransform.AccInputTransform;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;



public class AggInputTransform<I, J, O,
	SUBACC extends Accumulator<J, O>, SUBAGG extends ParallelAggregator<J, O, SUBACC>>
	implements ParallelAggregator<I, O, AccInputTransform<I, J, O, SUBACC>>, Serializable
{
	private static final long serialVersionUID = 0;

	public static interface AccInputTransform<I, J, O, SUBACC extends Accumulator<J, O>>
		extends AccWrapper<I, O, SUBACC> { }

	
	protected SUBAGG subAgg;
	protected Function<? super I, ? extends J> inputTransform;
	
	public AggInputTransform(SUBAGG subAgg, Function<? super I, ? extends J> inputTransform) {
		super();
		this.subAgg = subAgg;
		this.inputTransform = inputTransform;
	}

	@Override
	public AccInputTransform<I, J, O, SUBACC> createAccumulator() {
		SUBACC subAcc = subAgg.createAccumulator();
		
		return new AccTransformInputImpl(subAcc, inputTransform);
	}

	@Override
	public AccInputTransform<I, J, O, SUBACC> combine(AccInputTransform<I, J, O, SUBACC> a,
			AccInputTransform<I, J, O, SUBACC> b) {
		SUBACC accA = a.getSubAcc();
		SUBACC accB = b.getSubAcc();
		SUBACC combined = subAgg.combine(accA, accB);
		
		return new AccTransformInputImpl(combined, inputTransform); 
	}
	
	public class AccTransformInputImpl
		implements AccInputTransform<I, J, O, SUBACC>, Serializable
	{
		private static final long serialVersionUID = 0;

		protected SUBACC subAcc;
		protected Function<? super I, ? extends J> inputTransform;
		
		public AccTransformInputImpl(SUBACC subAcc, Function<? super I, ? extends J> inputTransform) {
			super();
			this.subAcc = subAcc;
			this.inputTransform = inputTransform;
		}
		
		@Override
		public void accumulate(I input) {
			J transformedInput = inputTransform.apply(input);
			subAcc.accumulate(transformedInput);
		}

		@Override
		public SUBACC getSubAcc() {
			return subAcc;
		}
		
		@Override
		public O getValue() {
			return subAcc.getValue();
		}
	}

}
