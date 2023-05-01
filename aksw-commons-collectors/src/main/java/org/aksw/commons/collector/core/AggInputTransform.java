package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.BiFunction;

import org.aksw.commons.collector.core.AggInputTransform.AccInputTransform;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;



public class AggInputTransform<I, E, J, O,
    SUBACC extends Accumulator<J, E, O>, SUBAGG extends ParallelAggregator<J, E, O, SUBACC>>
    implements ParallelAggregator<I, E, O, AccInputTransform<I, E, J, O, SUBACC>>, Serializable
{
    private static final long serialVersionUID = 0;

    public static interface AccInputTransform<I, E, J, O, SUBACC extends Accumulator<J, E, O>>
        extends AccWrapper<I, E, O, SUBACC> { }


    protected SUBAGG subAgg;
    protected BiFunction<? super I, E, ? extends J> inputTransform;

    public AggInputTransform(SUBAGG subAgg, BiFunction<? super I, E, ? extends J> inputTransform) {
        super();
        this.subAgg = subAgg;
        this.inputTransform = inputTransform;
    }

    @Override
    public AccInputTransform<I, E, J, O, SUBACC> createAccumulator() {
        SUBACC subAcc = subAgg.createAccumulator();

        return new AccTransformInputImpl(subAcc, inputTransform);
    }

    @Override
    public AccInputTransform<I, E, J, O, SUBACC> combine(AccInputTransform<I, E, J, O, SUBACC> a,
            AccInputTransform<I, E, J, O, SUBACC> b) {
        SUBACC accA = a.getSubAcc();
        SUBACC accB = b.getSubAcc();
        SUBACC combined = subAgg.combine(accA, accB);

        return new AccTransformInputImpl(combined, inputTransform);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inputTransform == null) ? 0 : inputTransform.hashCode());
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
        AggInputTransform<?, ?, ?, ?, ?, ?> other = (AggInputTransform<?, ?, ?, ?, ?, ?>) obj;
        if (inputTransform == null) {
            if (other.inputTransform != null)
                return false;
        } else if (!inputTransform.equals(other.inputTransform))
            return false;
        if (subAgg == null) {
            if (other.subAgg != null)
                return false;
        } else if (!subAgg.equals(other.subAgg))
            return false;
        return true;
    }




    public class AccTransformInputImpl
        implements AccInputTransform<I, E, J, O, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected SUBACC subAcc;
        protected BiFunction<? super I, E, ? extends J> inputTransform;

        public AccTransformInputImpl(SUBACC subAcc, BiFunction<? super I, E, ? extends J> inputTransform) {
            super();
            this.subAcc = subAcc;
            this.inputTransform = inputTransform;
        }

        @Override
        public void accumulate(I input, E env) {
            J transformedInput = inputTransform.apply(input, env);
            subAcc.accumulate(transformedInput, env);
        }

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
            result = prime * result + ((inputTransform == null) ? 0 : inputTransform.hashCode());
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
            AccTransformInputImpl other = (AccTransformInputImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (inputTransform == null) {
                if (other.inputTransform != null)
                    return false;
            } else if (!inputTransform.equals(other.inputTransform))
                return false;
            if (subAcc == null) {
                if (other.subAcc != null)
                    return false;
            } else if (!subAcc.equals(other.subAcc))
                return false;
            return true;
        }

        private AggInputTransform<?, ?, ?, ?, ?, ?> getEnclosingInstance() {
            return AggInputTransform.this;
        }
    }

}
