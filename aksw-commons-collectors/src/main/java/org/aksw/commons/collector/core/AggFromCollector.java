package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.stream.Collector;

import org.aksw.commons.collector.core.AggFromCollector.AccFromCollector;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * An aggregator from a collector.
 * Together with SerializableCollectorImpl a bridge is formed to
 * construct parallelizable aggregators from custom lambdas;
 *
 *
 *
 */
public class AggFromCollector<I, E, O, A>
    implements ParallelAggregator<I, E, O, AccFromCollector<I, E, O, A>>, Serializable
{
    private static final long serialVersionUID = 0L;

    public interface AccFromCollector<I, E, O, A> extends Accumulator<I, E, O> {
        A getAccumulator();
    }

    protected Collector<I, A, O> collector;

    public AggFromCollector(Collector<I, A, O> collector) {
        super();
        this.collector = collector;
    }

    @Override
    public AccFromCollector<I, E, O, A> createAccumulator() {
        return new AccFromCollectorImpl(collector.supplier().get());
    }

    @Override
    public AccFromCollector<I, E, O, A> combine(AccFromCollector<I, E, O, A> a, AccFromCollector<I, E, O, A> b) {
        return new AccFromCollectorImpl(collector.combiner().apply(a.getAccumulator(), b.getAccumulator()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collector == null) ? 0 : collector.hashCode());
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
        AggFromCollector<?, ?, ?, ?> other = (AggFromCollector<?, ?, ?, ?>) obj;
        if (collector == null) {
            if (other.collector != null)
                return false;
        } else if (!collector.equals(other.collector))
            return false;
        return true;
    }



    public class AccFromCollectorImpl
        implements AccFromCollector<I, E, O, A>, Serializable {

        private static final long serialVersionUID = 1L;

        protected A accumulator;

        public AccFromCollectorImpl(A accumulator) {
            super();
            this.accumulator = accumulator;
        }

        @Override
        public A getAccumulator() {
            return accumulator;
        }

        @Override
        public void accumulate(I input, E env) {
            collector.accumulator().accept(accumulator, input);
        }

        @Override
        public O getValue() {
            return collector.finisher().apply(accumulator);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((accumulator == null) ? 0 : accumulator.hashCode());
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
            AccFromCollectorImpl other = (AccFromCollectorImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (accumulator == null) {
                if (other.accumulator != null)
                    return false;
            } else if (!accumulator.equals(other.accumulator))
                return false;
            return true;
        }

        private AggFromCollector<?, ?, ?, ?> getEnclosingInstance() {
            return AggFromCollector.this;
        }
    }
}
