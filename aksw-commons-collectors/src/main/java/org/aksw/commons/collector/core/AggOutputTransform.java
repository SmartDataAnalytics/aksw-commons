package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.Function;

import org.aksw.commons.collector.core.AggOutputTransform.AccOutputTransform;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


public class AggOutputTransform<I, E, O, P,
        SUBACC extends Accumulator<I, E, O>,
        SUBAGG extends ParallelAggregator<I, E, O, SUBACC>
    >
    implements ParallelAggregator<I, E, P, AccOutputTransform<I, E, O, P, SUBACC>>, Serializable
{
    private static final long serialVersionUID = 0;

    public static interface AccOutputTransform<I, E, O, P, SUBACC extends Accumulator<I, E, O>>
        extends AccWrapper<I, E, P, SUBACC> { }

    protected SUBAGG subAgg;
    protected Function<? super O, ? extends P> outputTransform;

    public AggOutputTransform(SUBAGG subAgg, Function<? super O, ? extends P> outputTransform) {
        super();
        this.subAgg = subAgg;
        this.outputTransform = outputTransform;
    }

    @Override
    public AccOutputTransform<I, E, O, P, SUBACC> createAccumulator() {
        SUBACC subAcc = subAgg.createAccumulator();

        return new AccOutputTransformImpl(subAcc, outputTransform);
    }

    @Override
    public AccOutputTransform<I, E, O, P, SUBACC> combine(AccOutputTransform<I, E, O, P, SUBACC> a,
            AccOutputTransform<I, E, O, P, SUBACC> b) {
        SUBACC accA = a.getSubAcc();
        SUBACC accB = b.getSubAcc();
        SUBACC combined = subAgg.combine(accA, accB);

        return new AccOutputTransformImpl(combined, outputTransform);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((outputTransform == null) ? 0 : outputTransform.hashCode());
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
        AggOutputTransform<?, ?, ?, ?, ?, ?> other = (AggOutputTransform<?, ?, ?, ?, ?, ?>) obj;
        if (outputTransform == null) {
            if (other.outputTransform != null)
                return false;
        } else if (!outputTransform.equals(other.outputTransform))
            return false;
        if (subAgg == null) {
            if (other.subAgg != null)
                return false;
        } else if (!subAgg.equals(other.subAgg))
            return false;
        return true;
    }


    public class AccOutputTransformImpl
        implements AccOutputTransform<I, E, O, P, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected SUBACC subAcc;
        protected Function<? super O, ? extends P> outputTransform;

        public AccOutputTransformImpl(SUBACC subAcc, Function<? super O, ? extends P> outputTransform) {
            super();
            this.subAcc = subAcc;
            this.outputTransform = outputTransform;
        }

        @Override
        public void accumulate(I input, E env) {
            subAcc.accumulate(input, env);
        }

        @Override
        public SUBACC getSubAcc() {
            return subAcc;
        }

        @Override
        public P getValue() {
            O rawResult = subAcc.getValue();
            P result = outputTransform.apply(rawResult);
            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((outputTransform == null) ? 0 : outputTransform.hashCode());
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
            AccOutputTransformImpl other = (AccOutputTransformImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (outputTransform == null) {
                if (other.outputTransform != null)
                    return false;
            } else if (!outputTransform.equals(other.outputTransform))
                return false;
            if (subAcc == null) {
                if (other.subAcc != null)
                    return false;
            } else if (!subAcc.equals(other.subAcc))
                return false;
            return true;
        }

        private AggOutputTransform<?, ?, ?, ?, ?, ?> getEnclosingInstance() {
            return AggOutputTransform.this;
        }
    }
}
