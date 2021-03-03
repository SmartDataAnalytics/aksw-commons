package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.aksw.commons.collector.core.AggInputBroadcast.AccInputBroadcast;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * An aggregator that broadcasts its input to two sub-aggregators that accept the same input.
 * 
 * Serves the purpose to perform two independent type safe aggregations on the same input in a single pass.
 * 
 * @author raven
 *
 * @param <I> Input type
 * @param <O1> output type of first aggregator
 * @param <O2> output type of second aggregator
 * @param <SUBACC1> accumulator type of first aggregator
 * @param <SUBAGG1> the type of the first aggregator
 * @param <SUBACC2> accumulator type of second aggregator
 * @param <SUBAGG2> the type of the second aggregator
 */
public class AggInputBroadcast<I, O1, O2,
	SUBACC1 extends Accumulator<I, O1>,
	SUBAGG1 extends ParallelAggregator<I, O1, SUBACC1>,
	SUBACC2 extends Accumulator<I, O2>,
	SUBAGG2 extends ParallelAggregator<I, O2, SUBACC2>
	>
	implements ParallelAggregator<I, Entry<O1, O2>, AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2>>, Serializable
{
	private static final long serialVersionUID = 0;


	public static interface AccInputBroadcast<I, O1, O2,
		SUBACC1 extends Accumulator<I, O1>,
		SUBACC2 extends Accumulator<I, O2>>
	extends Accumulator<I, Entry<O1, O2>> {
		SUBACC1 getSubAcc1();
		SUBACC2 getSubAcc2();
	}
	
	protected SUBAGG1 subAgg1;
	protected SUBAGG2 subAgg2;
	
	public AggInputBroadcast(SUBAGG1 subAgg1, SUBAGG2 subAgg2) {
		super();
		this.subAgg1 = subAgg1;
		this.subAgg2 = subAgg2;
	}
	
	@Override
	public AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2> createAccumulator() {
		return new AccInputBroadcastImpl(subAgg1.createAccumulator(), subAgg2.createAccumulator());
	}
	
	@Override
	public AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2> combine(
			AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2> a,
			AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2> b) {

		SUBACC1 newSubAcc1 = subAgg1.combine(a.getSubAcc1(), b.getSubAcc1());
		SUBACC2 newSubAcc2 = subAgg2.combine(a.getSubAcc2(), b.getSubAcc2());
		
		return new AccInputBroadcastImpl(newSubAcc1, newSubAcc2); 
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subAgg1 == null) ? 0 : subAgg1.hashCode());
		result = prime * result + ((subAgg2 == null) ? 0 : subAgg2.hashCode());
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
		AggInputBroadcast<?, ?, ?, ?, ?, ?, ?> other = (AggInputBroadcast<?, ?, ?, ?, ?, ?, ?>) obj;
		if (subAgg1 == null) {
			if (other.subAgg1 != null)
				return false;
		} else if (!subAgg1.equals(other.subAgg1))
			return false;
		if (subAgg2 == null) {
			if (other.subAgg2 != null)
				return false;
		} else if (!subAgg2.equals(other.subAgg2))
			return false;
		return true;
	}



	public class AccInputBroadcastImpl
		implements AccInputBroadcast<I, O1, O2, SUBACC1, SUBACC2>, Serializable
	{
		private static final long serialVersionUID = 0;
		
		protected SUBACC1 subAcc1;
		protected SUBACC2 subAcc2;
		
		
		
		public AccInputBroadcastImpl(SUBACC1 subAcc1, SUBACC2 subAcc2) {
			super();
			this.subAcc1 = subAcc1;
			this.subAcc2 = subAcc2;
		}
	
		@Override
		public void accumulate(I input) {
			subAcc1.accumulate(input);
			subAcc2.accumulate(input);
		}
	
		@Override
		public Entry<O1, O2> getValue() {
			O1 o1 = subAcc1.getValue();
			O2 o2 = subAcc2.getValue();
			
			return new SimpleEntry<>(o1, o2);
		}
	
		@Override
		public SUBACC1 getSubAcc1() {
			return subAcc1;
		}

		@Override
		public SUBACC2 getSubAcc2() {
			return subAcc2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((subAcc1 == null) ? 0 : subAcc1.hashCode());
			result = prime * result + ((subAcc2 == null) ? 0 : subAcc2.hashCode());
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
			@SuppressWarnings("unchecked")
			AccInputBroadcastImpl other = (AccInputBroadcastImpl) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (subAcc1 == null) {
				if (other.subAcc1 != null)
					return false;
			} else if (!subAcc1.equals(other.subAcc1))
				return false;
			if (subAcc2 == null) {
				if (other.subAcc2 != null)
					return false;
			} else if (!subAcc2.equals(other.subAcc2))
				return false;
			return true;
		}

		private AggInputBroadcast<?, ?, ?, ?, ?, ?, ?> getEnclosingInstance() {
			return AggInputBroadcast.this;
		}
	}
}
