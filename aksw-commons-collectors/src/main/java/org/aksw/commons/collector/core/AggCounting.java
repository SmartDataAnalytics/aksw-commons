package org.aksw.commons.collector.core;

import java.io.Serializable;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * Aggregator whose accumulator count the number of seen input objects.
 * 
 * @author raven
 *
 * @param <I> The input type
 */
public class AggCounting<I>
	implements ParallelAggregator<I, Long, Accumulator<I, Long>>, Serializable
{
	private static final long serialVersionUID = 0;


	@Override
	public Accumulator<I, Long> createAccumulator() {
		return new AccCounting(0);
	}

	@Override
	public Accumulator<I, Long> combine(Accumulator<I, Long> a, Accumulator<I, Long> b) {
		long count1 = a.getValue();
		long count2 = b.getValue();
		long newCount = count1 + count2;
		
		return new AccCounting(newCount);
	}

	@Override
	public int hashCode() {
		return 41;
	}
	
	@Override
	public boolean equals(Object other) {
		return other == null ? false : getClass() == other.getClass();
	}
	
	public class AccCounting
		implements Accumulator<I, Long>, Serializable
	{
		private static final long serialVersionUID = 0;

		protected long count = 0;
		
		public AccCounting(long count) {
			super();
			this.count = count;
		}

		@Override
		public void accumulate(I binding) {
			++count;
		}

		@Override
		public Long getValue() {
			return count;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + (int) (count ^ (count >>> 32));
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
			AccCounting other = (AccCounting) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (count != other.count)
				return false;
			return true;
		}

		private AggCounting<?> getEnclosingInstance() {
			return AggCounting.this;
		}
	}
}
