package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

/**
 * Convert the output value of an aggregator - this fulfills the role of the 'finisher' in a java8 collector.
 * Note the difference between outputTransform and finish: The former is a per-accumulator operation which can be performed concurrently,
 * whereas finish is a post-processing of the overall result.
 */
public class AggFinish<B, I, E, O, C extends Aggregator<B, E, I>>
    implements Aggregator<B, E, O>, Serializable
{
    private static final long serialVersionUID = 1L;

    private C subAgg;
    private Function<? super I, O> transform;


    public AggFinish(C subAgg, Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = transform;
    }

    @Override
    public Accumulator<B, E, O> createAccumulator() {
        Accumulator<B, E, I> baseAcc = subAgg.createAccumulator();
        Accumulator<B, E, O> result = new AccFinish<>(baseAcc, transform);
        return result;
    }

    public C getSubAgg() {
        return subAgg;
    }

    public Function<? super I, O> getTransform() {
        return transform;
    }

    public static <B, I, E, O, C extends Aggregator<B, E, I>> AggFinish<B, I, E, O, C> create(C subAgg, Function<? super I, O> transform) {
        AggFinish<B, I, E, O, C> result = new AggFinish<>(subAgg, transform);
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


    public static class AccFinish<I, E, O, V>
        implements Accumulator<I, E, V>, Serializable
    {
        private static final long serialVersionUID = 1L;

        protected Accumulator<I, E, O> subAcc;
        protected Function<? super O, V> transform;

        public AccFinish(Accumulator<I, E, O> subAcc, Function<? super O, V> transform) {
            this.subAcc = subAcc;
            this.transform = transform;
        }

        @Override
        public void accumulate(I input, E env) {
            subAcc.accumulate(input, env);
        }

        public Accumulator<I, E, O> getSubAcc() {
            return subAcc;
        }

        @Override
        public V getValue() {
            O accValue = subAcc.getValue();
            V result = transform.apply(accValue);
            return result;
        }

        /** Shortcut for {@code getEnclosingInstance().getTransform()} */
        public Function<? super O, V> getTransform() {
            return transform;
        }

        @Override
        public int hashCode() {
            return Objects.hash(subAcc, transform);
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
            return Objects.equals(subAcc, other.subAcc) && Objects.equals(transform, other.transform);
        }

//	    public static <B, I, O> Accumulator<B, O> create(Accumulator<B, I> subAcc, Function<? super I, O> transform) {
//	        Accumulator<B, O> result = create(subAcc, transform);
//	        return result;
//	    }
    }

}
