package org.aksw.commons.collector.core;


import java.io.Serializable;
import java.util.function.Predicate;

import org.aksw.commons.collector.core.AggInputFilter.AccInputFilter;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * Wrap an aggregator such that inputs are passed through a predicate.
 * The predicate must evaluate to true for an item to be forwarded to the underyling accumulator.
 *
 * @author raven
 *
 * @param <I>
 * @param <O>
 * @param <SUBACC>
 * @param <SUBAGG>
 */
public class AggInputFilter<
        I, E,
        O,
        SUBACC extends Accumulator<I, E, O>,
        SUBAGG extends ParallelAggregator<I, E, O, SUBACC>
    >
    implements ParallelAggregator<I, E, O, AccInputFilter<I, E, O, SUBACC>>, Serializable
{
    private static final long serialVersionUID = 0;

    public static interface AccInputFilter<I, E, O, SUBACC extends Accumulator<I, E, O>>
        extends AccWrapper<I, E, O, SUBACC> {
    }

    protected SUBAGG subAgg;
    protected Predicate<? super I> inputFilter;

    public AggInputFilter(SUBAGG subAgg, Predicate<? super I> inputFilter) {
        super();
        this.subAgg = subAgg;
        this.inputFilter = inputFilter;
    }

    @Override
    public AccInputFilter<I, E, O, SUBACC> createAccumulator() {
        SUBACC subAcc = subAgg.createAccumulator();

        return new AccFilterInputImpl(subAcc, inputFilter);
    }


    @Override
    public AccInputFilter<I, E, O, SUBACC> combine(AccInputFilter<I, E, O, SUBACC> a,
            AccInputFilter<I, E, O, SUBACC> b) {
//		SUBACC accA = a.getValue();
//		SUBACC accB = b.getValue();
        SUBACC accA = a.getSubAcc();
        SUBACC accB = b.getSubAcc();
        SUBACC combined = subAgg.combine(accA, accB);

        return new AccFilterInputImpl(combined, inputFilter);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputFilter == null) ? 0 : inputFilter.hashCode());
        result = prime * result + ((subAgg == null) ? 0 : subAgg.hashCode());
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
        AggInputFilter<?, ?, ?, ?, ?> other = (AggInputFilter<?, ?, ?, ?, ?>) obj;
        if (inputFilter == null) {
            if (other.inputFilter != null)
                return false;
        } else if (!inputFilter.equals(other.inputFilter))
            return false;
        if (subAgg == null) {
            if (other.subAgg != null)
                return false;
        } else if (!subAgg.equals(other.subAgg))
            return false;
        return true;
    }

//	@Override
//	public O getValue(AccFilterInput<I, O, SUBACC> a) {
//		return subAgg.getValue(a.getValue());
//	}





    public class AccFilterInputImpl
        implements AccInputFilter<I, E, O, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected SUBACC subAcc;
        protected Predicate<? super I> inputFilter;

        public AccFilterInputImpl(SUBACC subAcc, Predicate<? super I> inputFilter) {
            super();
            this.subAcc = subAcc;
            this.inputFilter = inputFilter;
        }

        @Override
        public void accumulate(I input, E env) {
            boolean isAccepted = inputFilter.test(input);

            if (isAccepted) {
                subAcc.accumulate(input, env);
            }
        }

//		@Override
//		public SUBACC getValue() {
//			return subAcc.getValue();
//		}

        @Override
        public SUBACC getSubAcc() {
            return subAcc;
        }

        @Override
        public O getValue() {
            return subAcc.getValue();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((inputFilter == null) ? 0 : inputFilter.hashCode());
            result = prime * result + ((subAcc == null) ? 0 : subAcc.hashCode());
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
            AccFilterInputImpl other = (AccFilterInputImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (inputFilter == null) {
                if (other.inputFilter != null)
                    return false;
            } else if (!inputFilter.equals(other.inputFilter))
                return false;
            if (subAcc == null) {
                if (other.subAcc != null)
                    return false;
            } else if (!subAcc.equals(other.subAcc))
                return false;
            return true;
        }

        private AggInputFilter<?, ?, ?, ?, ?> getEnclosingInstance() {
            return AggInputFilter.this;
        }
    }
}
