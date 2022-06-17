package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.collector.core.AggErrorHandler.AccError;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


/** Wraps another aggregator such that if accumulation fails with an exception
 *  an error counter is increased. If the error counter is non zero then the accumulated value will always be null */
public class AggErrorHandler<I, O,
    SUBACC extends Accumulator<I, O>, SUBAGG extends ParallelAggregator<I, O, SUBACC>>
    implements ParallelAggregator<I, O, AccError<I, O, SUBACC>>, Serializable
{
    // private static final Logger logger = LoggerFactory.crea

    private static final long serialVersionUID = 0;

    public static interface AccError<I, O, SUBACC extends Accumulator<I, O>>
        extends AccWrapper<I, O, SUBACC>
    {
        long getErrorCount();
    }

    protected SUBAGG subAgg;
    protected Function<AccError<I, O, SUBACC> , O> errorValueExtractor;
    protected Consumer<? super Throwable> errorCallback;

    /** Whether to delegate accumulate calls to the sub-accumulator despite error.
     * BEWARE when using en errorValueExtractor: If subAccDespiteError is false then combining accumulators will result in a null accumulator
     */
    protected boolean subAccDespiteError;

    public AggErrorHandler(
            SUBAGG subAgg,
            boolean subAccDespiteError,
            Consumer<? super Throwable> errorCallback,
            Function<AccError<I, O, SUBACC> , O> errorValueExtractor) {
        super();
        this.subAgg = subAgg;
        this.subAccDespiteError = subAccDespiteError;
        this.errorCallback = errorCallback;
        this.errorValueExtractor = errorValueExtractor;

    }

    @Override
    public AccError<I, O, SUBACC> createAccumulator() {
        SUBACC subAcc = subAgg.createAccumulator();
        return new AccErrorImpl(subAcc, 0);
    }

    @Override
    public AccError<I, O, SUBACC> combine(AccError<I, O, SUBACC> a,
            AccError<I, O, SUBACC> b) {

        SUBACC accA = a.getSubAcc();
        SUBACC accB = b.getSubAcc();

        long totalErrorCount = a.getErrorCount() + b.getErrorCount(); // Should be saturated add
        SUBACC subAcc = totalErrorCount == 0 || subAccDespiteError
                ? subAgg.combine(accA, accB)
                : null;

        return new AccErrorImpl(subAcc, totalErrorCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCallback, errorValueExtractor, subAccDespiteError, subAgg);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AggErrorHandler<?, ?, ?, ?> other = (AggErrorHandler<?, ?, ?, ?>) obj;
        return Objects.equals(errorCallback, other.errorCallback)
                && Objects.equals(errorValueExtractor, other.errorValueExtractor)
                && subAccDespiteError == other.subAccDespiteError && Objects.equals(subAgg, other.subAgg);
    }




    public class AccErrorImpl
        implements AccError<I, O, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected SUBACC subAcc;
        protected long errorCount;

        public AccErrorImpl(SUBACC subAcc, long errorCount) {
            super();
            this.subAcc = subAcc;
            this.errorCount = errorCount;
        }

        @Override
        public void accumulate(I input) {
            try {
                if (errorCount == 0 || subAccDespiteError) {
                    subAcc.accumulate(input);
                }
            } catch (Exception e) {
                ++errorCount;
                if (errorCallback != null) {
                    errorCallback.accept(e);
                }
            }
        }

        @Override
        public SUBACC getSubAcc() {
            return subAcc;
        }

        @Override
        public long getErrorCount() {
            return errorCount;
        }

        @Override
        public O getValue() {
            O result = errorCount == 0
                    ? subAcc.getValue()
                    : errorValueExtractor == null
                        ? null
                        : errorValueExtractor.apply(this);

            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(errorCount, subAcc);
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
            AccErrorImpl other = (AccErrorImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            return errorCount == other.errorCount && Objects.equals(subAcc, other.subAcc);
        }

        private AggErrorHandler<?, ?, ?, ?> getEnclosingInstance() {
            return AggErrorHandler.this;
        }
    }

}
