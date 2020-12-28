package org.aksw.commons.util.healthcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.aksw.commons.util.function.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a configurable (health check) action at fixed intervals until this action longer raises
 * an exception. Fatal conditions can be configured to abort the health check early.
 *
 * @author raven
 *
 */
public class HealthcheckRunner<T>
    implements Callable<T>, Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(HealthcheckRunner.class);

    protected long retryCount;
    protected long interval;
    protected TimeUnit intervalTimeUnit;
    protected Callable<? extends T> action;
    protected List<Predicate<? super Throwable>> fatalConditions;
    protected List<Supplier<Boolean>> continuationConditions;
    
    /* Runtime attributes */
    protected boolean isAborted;
    
    /** The thread that called {@link #run()} */
    protected volatile Thread thread;

    public HealthcheckRunner(long retryCount, long interval, TimeUnit intervalTimeUnit,
            Callable<? extends T> action, List<Predicate<? super Throwable>> fatalConditions, List<Supplier<Boolean>> continuationConditions) {
        super();
        this.retryCount = retryCount;
        this.interval = interval;
        this.intervalTimeUnit = intervalTimeUnit;
        this.action = action;
        this.fatalConditions = fatalConditions;
        this.continuationConditions = continuationConditions;
    }

    public static Builder<Void> builder() {
        return new Builder<Void>();
    }

    public static class Builder<X> {
        protected long retryCount;
        protected long interval;
        protected TimeUnit intervalTimeUnit;
        protected Callable<X> action;
        protected List<Predicate<? super Throwable>> fatalConditions;
        protected List<Supplier<Boolean>> continuationConditions;

        public Builder() {
            this(60l, 1l, TimeUnit.SECONDS, null);
        }

        public Builder(long retryCount, long interval, TimeUnit intervalUnit, Callable<X> action) {
            super();
            this.retryCount = retryCount;
            this.interval = interval;
            this.intervalTimeUnit = intervalUnit;
            this.action = action;
            this.fatalConditions = null;
            this.continuationConditions = null;
        }

        public long getRetryCount() {
            return retryCount;
        }

        public Builder<X> setRetryCount(long retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public long getInterval() {
            return interval;
        }

        public Builder<X> setInterval(long interval, TimeUnit timeUnit) {
            Objects.requireNonNull(timeUnit, "TimeUnit must not be null");

            this.interval = interval;
            this.intervalTimeUnit = timeUnit;
            return this;
        }

        public TimeUnit getIntervalTimeUnit() {
            return intervalTimeUnit;
        }

		public Callable<X> getAction() {
            return action;
        }

		public <Y> Builder<Y> setAction(Callable<Y> action) {
            @SuppressWarnings("unchecked")
			Builder<Y> view = (Builder<Y>)this;
            view.action = action;
            return view;
        }

        public Builder<Void> setAction(ThrowingRunnable action) {
        	Callable<Void> callable = () -> { action.run(); return null; };
            return setAction(callable);
        }

        
        public List<Predicate<? super Throwable>> getFatalConditions() {
            return fatalConditions;
        }

        public Builder<X> addFatalCondition(Predicate<? super Throwable> fatalCondition) {
            if (this.fatalConditions == null) {
                this.fatalConditions = new ArrayList<>(1);
            }

            this.fatalConditions.add(fatalCondition);
            return this;
        }

        /** If the result of <b>any</b> continuation condition is false then the retry checks are aborted */
        public Builder<X> addContinuationCondition(Supplier<Boolean> continuationCondition) {
            if (this.continuationConditions == null) {
                this.continuationConditions = new ArrayList<>(1);
            }

            this.continuationConditions.add(continuationCondition);
            return this;
        }


        public HealthcheckRunner<X> build() {
            Objects.requireNonNull(action);
            return new HealthcheckRunner<X>(
                    retryCount,
                    interval,
                    intervalTimeUnit,
                    action,
                    (fatalConditions == null ? Collections.emptyList() : fatalConditions),
                    (continuationConditions == null ? Collections.emptyList() : continuationConditions)
                    );
        }
    }

    public void abort() {
    	synchronized (this) {
			if (thread != null) {
				thread.interrupt();
			}
	    	isAborted = true;
		}
    }
    
    public boolean isAborted() {
    	return isAborted;
    }

    public boolean isConditionallyAborted() {
    	boolean result = continuationConditions.stream().anyMatch(c -> Boolean.FALSE.equals(c.get()));
    	return result;
    }

    /** Prefer this method if the result should be ignored */
    @Override
    public void run() {
    	call();
    }
    
    @Override
    public T call() {
    	synchronized (this) {
			if (thread != null) {
				throw new IllegalStateException("run() was already called");				
			}
			thread = Thread.currentThread();
		}

        // Wait for the health check to succeed the first time
    	T result = null;
    	
        boolean success = false;
        Exception mostRecentException = null;

        int i = 0;
        boolean wasManuallyAborted = false;
        boolean wasConditionallyAborted =  false;
        
     // Beware of shortcut evaluation: Manual abort takes precedence over conditional abort
        for(; i < retryCount &&
        		!(wasManuallyAborted = isAborted()) &&
        		!(wasConditionallyAborted = isConditionallyAborted()) &&
        		!Thread.interrupted(); ++i) {
        	
            boolean isLastIteration = i + 1 >= retryCount;

            try {
                result = action.call();
                logger.info("Health check status: success");
                success = true;
                break;
            } catch(Exception e) {

                mostRecentException = e;

                // Check if the exception is fatal
                boolean isFatal = fatalConditions.stream().anyMatch(fatal -> fatal.test(e));
                if (isFatal) {
                    logger.info("Health check status: Encountered fatal condition, aborting with exception", e);
                    throw new RuntimeException(e);
                } else {
                    logger.info("Health check status: not ok - "
                                + (retryCount >= Long.MAX_VALUE ? "" : (retryCount - i) + " retries remaining. ")
                                + (isLastIteration ? "" : "Retrying in " + interval + " " + intervalTimeUnit));
                }
            }

            if (!isLastIteration) {
                try {
                    intervalTimeUnit.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException("aborted", e);
                }
            }
        }

        if (!success) {
        	if (wasManuallyAborted) {
        		throw new RuntimeException("Aborted up after " + i + " failed health checks", mostRecentException);        		
        	} else if (wasConditionallyAborted) {
        		throw new RuntimeException("Aborted due to a condition not satisfied up after " + i + " failed health checks", mostRecentException);        		
        	} else {
        		throw new RuntimeException("Giving up after " + i + " failed health checks", mostRecentException);
        	}
        }
        
        return result;
    }
}
