package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

/**
 * Convert the output value of an aggregator - this fulfills the role of the 'finisher' in a java8 collector.
 * Note the difference between outputTransform and finish: The former is a per-accumulator operation which can be performed concurrently,
 * whereas finish is a post-processing of the overall result.
 */
public class AggFinish<B, I, O, C extends Aggregator<B, I>>
    implements Aggregator<B, O>, Serializable
{
    private C subAgg;
    private Function<? super I, O> transform;


    public AggFinish(C subAgg, Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = transform;
    }

    @Override
    public Accumulator<B, O> createAccumulator() {
        Accumulator<B, I> baseAcc = subAgg.createAccumulator();
        Accumulator<B, O> result = new AccFinish(baseAcc);
        return result;
    }

    public static <B, I, O, C extends Aggregator<B, I>> AggFinish<B, I, O, C> create(C subAgg, Function<? super I, O> transform) {
        AggFinish<B, I, O, C> result = new AggFinish<>(subAgg, transform);
        return result;
    }


//    public static <I, O> AggTransform<I, O> create(Agg<I> subAgg, Function<I, O> transform) {
//        AggTransform<I, O> result = new AggTransform<I, O>(subAgg, transform);
//        return result;
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subAgg == null) ? 0 : subAgg.hashCode());
        result = prime * result + ((transform == null) ? 0 : transform.hashCode());
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
        AggFinish other = (AggFinish) obj;
        if (subAgg == null) {
            if (other.subAgg != null)
                return false;
        } else if (!subAgg.equals(other.subAgg))
            return false;
        if (transform == null) {
            if (other.transform != null)
                return false;
        } else if (!transform.equals(other.transform))
            return false;
        return true;
    }


    public class AccFinish
        implements Accumulator<B, O>, Serializable
    {
        private static final long serialVersionUID = 1L;

        protected Accumulator<B, I> subAcc;

        public AccFinish(Accumulator<B, I> subAcc) {
            this.subAcc = subAcc;
        }

        @Override
        public void accumulate(B binding) {
            subAcc.accumulate(binding);
        }

        @Override
        public O getValue() {
            I input = subAcc.getValue();
            O result = transform.apply(input);
            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
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
            AccFinish other = (AccFinish) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (subAcc == null) {
                if (other.subAcc != null)
                    return false;
            } else if (!subAcc.equals(other.subAcc))
                return false;
            return true;
        }

        private AggFinish getEnclosingInstance() {
            return AggFinish.this;
        }

//	    public static <B, I, O> Accumulator<B, O> create(Accumulator<B, I> subAcc, Function<? super I, O> transform) {
//	        Accumulator<B, O> result = create(subAcc, transform);
//	        return result;
//	    }
    }

}