package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


/**
 * Aggregator whose accumulators apply a reduce operation to their
 * value and their input to compute their new value.
 * Accumulators are initialized with a zero element.
 * Each accumulator obtains its zero element from a supplier which allows
 * for reduce operations that mutate that element.
 *
 * Used as a basis for: min, max, sum (which in turn are the basis for e.g. avg)
 */
public class AggReduce<I, E>
    implements ParallelAggregator<I, E, I, Accumulator<I, E, I>>, Serializable
{
    private static final long serialVersionUID = 0L;

    protected Supplier<I> zeroElementSupplier;
    protected BinaryOperator<I> plusOperator;

    public AggReduce(Supplier<I> zeroElementSupplier, BinaryOperator<I> plusOperator) {
        super();
        this.zeroElementSupplier = zeroElementSupplier;
        this.plusOperator = plusOperator;
    }

    @Override
    public Accumulator<I, E, I> createAccumulator() {
        I zeroElement = zeroElementSupplier.get();
        return new AccReduceImpl(zeroElement);
    }

    @Override
    public Accumulator<I, E, I> combine(Accumulator<I, E, I> a, Accumulator<I, E, I> b) {
        I va = a.getValue();
        I vb = b.getValue();
        I combinedValue = plusOperator.apply(va, vb);

        return new AccReduceImpl(combinedValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((plusOperator == null) ? 0 : plusOperator.hashCode());
        result = prime * result + ((zeroElementSupplier == null) ? 0 : zeroElementSupplier.hashCode());
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
        AggReduce<?, ?> other = (AggReduce<?, ?>) obj;
        if (plusOperator == null) {
            if (other.plusOperator != null)
                return false;
        } else if (!plusOperator.equals(other.plusOperator))
            return false;
        if (zeroElementSupplier == null) {
            if (other.zeroElementSupplier != null)
                return false;
        } else if (!zeroElementSupplier.equals(other.zeroElementSupplier))
            return false;
        return true;
    }



    public class AccReduceImpl
        implements Accumulator<I, E, I>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected I value;

        public AccReduceImpl(I value) {
            super();
            this.value = value;
        }

        @Override
        public void accumulate(I input, E env) {
            value = plusOperator.apply(value, input);
        }

        @Override
        public I getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            AccReduceImpl other = (AccReduceImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        private AggReduce<?, ?> getEnclosingInstance() {
            return AggReduce.this;
        }
    }
}

