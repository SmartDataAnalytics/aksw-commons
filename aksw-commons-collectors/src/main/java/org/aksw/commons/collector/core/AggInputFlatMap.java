package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;

import org.aksw.commons.collector.core.AggInputFlatMap.AccInputFlatMap;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


/**
 * Pass collection valued input to an accumulator that accepts only the individual items
 * For example, pass all nodes of a binding to an accumulator for nodes
 *
 * This operation is a variant of input transform, where the transform target
 * is an iterator. Each item of the iterator then passed to the accumulator.
 *
 */
public class AggInputFlatMap<I, E, J, O,
    SUBACC extends Accumulator<J, E, O>, SUBAGG extends ParallelAggregator<J, E, O, SUBACC>>
    implements ParallelAggregator<I, E, O, AccInputFlatMap<I, E, J, O, SUBACC>>, Serializable
{
    private static final long serialVersionUID = 0;

    public static interface AccInputFlatMap<I, E, J, O, SUBACC extends Accumulator<J, E, O>>
        extends AccWrapper<I, E, O, SUBACC> { }


    protected SUBAGG subAgg;
    protected Function<? super I, ? extends Iterator<? extends J>> inputTransform;

    public AggInputFlatMap(SUBAGG subAgg, Function<? super I, ? extends Iterator<? extends J>> inputTransform) {
        super();
        this.subAgg = subAgg;
        this.inputTransform = inputTransform;
    }

    @Override
    public AccInputFlatMap<I, E, J, O, SUBACC> createAccumulator() {
        SUBACC subAcc = subAgg.createAccumulator();

        return new AccTransformInputImpl(subAcc, inputTransform);
    }

    @Override
    public AccInputFlatMap<I, E, J, O, SUBACC> combine(AccInputFlatMap<I, E, J, O, SUBACC> a,
            AccInputFlatMap<I, E, J, O, SUBACC> b) {
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
        AggInputFlatMap<?, ?, ?, ?, ?, ?> other = (AggInputFlatMap<?, ?, ?, ?, ?, ?>) obj;
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
        implements AccInputFlatMap<I, E, J, O, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected SUBACC subAcc;
        protected Function<? super I, ? extends Iterator<? extends J>> inputTransform;

        public AccTransformInputImpl(SUBACC subAcc, Function<? super I, ? extends Iterator<? extends J>> inputTransform) {
            super();
            this.subAcc = subAcc;
            this.inputTransform = inputTransform;
        }

        @Override
        public void accumulate(I input, E env) {
            Iterator<? extends J> it = inputTransform.apply(input);
            while (it.hasNext()) {
                J item = it.next();
                subAcc.accumulate(item, env);
            }
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
            final int prime = 59;
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

        private AggInputFlatMap<?, ?, ?, ?, ?, ?> getEnclosingInstance() {
            return AggInputFlatMap.this;
        }
    }

}
