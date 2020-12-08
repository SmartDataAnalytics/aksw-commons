package org.aksw.commons.util.healthcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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
public class HealthcheckRunner
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(HealthcheckRunner.class);

    protected long retryCount;
    protected long interval;
    protected TimeUnit intervalTimeUnit;
    protected ThrowingRunnable action;
    protected List<Predicate<? super Throwable>> fatalConditions;

    public HealthcheckRunner(long retryCount, long interval, TimeUnit intervalTimeUnit,
            ThrowingRunnable action, List<Predicate<? super Throwable>> fatalConditions) {
        super();
        this.retryCount = retryCount;
        this.interval = interval;
        this.intervalTimeUnit = intervalTimeUnit;
        this.action = action;
        this.fatalConditions = fatalConditions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected long retryCount;
        protected long interval;
        protected TimeUnit intervalTimeUnit;
        protected ThrowingRunnable action;
        protected List<Predicate<? super Throwable>> fatalConditions;

        public Builder() {
            this(60l, 1l, TimeUnit.SECONDS, null);
        }

        public Builder(long retryCount, long interval, TimeUnit intervalUnit, ThrowingRunnable action) {
            super();
            this.retryCount = retryCount;
            this.interval = interval;
            this.intervalTimeUnit = intervalUnit;
            this.action = action;
            this.fatalConditions = null;
        }

        public long getRetryCount() {
            return retryCount;
        }

        public Builder setRetryCount(long retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public long getInterval() {
            return interval;
        }

        public Builder setInterval(long interval, TimeUnit timeUnit) {
            Objects.requireNonNull(timeUnit, "TimeUnit must not be null");

            this.interval = interval;
            this.intervalTimeUnit = timeUnit;
            return this;
        }

        public TimeUnit getIntervalTimeUnit() {
            return intervalTimeUnit;
        }

        public ThrowingRunnable getAction() {
            return action;
        }

        public Builder setAction(Callable<?> action) {
            this.action = () -> action.call();
            return this;
        }

        public Builder setAction(ThrowingRunnable action) {
            this.action = action;
            return this;
        }

        public List<Predicate<? super Throwable>> getFatalConditions() {
            return fatalConditions;
        }

        public Builder addFatalCondition(Predicate<? super Throwable> fatalCondition) {
            if (this.fatalConditions == null) {
                this.fatalConditions = new ArrayList<>(1);
            }

            this.fatalConditions.add(fatalCondition);
            return this;
        }


        public HealthcheckRunner build() {
            Objects.requireNonNull(action);
            return new HealthcheckRunner(
                    retryCount,
                    interval,
                    intervalTimeUnit,
                    action,
                    (fatalConditions == null ? Collections.emptyList() : fatalConditions)
                    );
        }
    }

//    public void abort() {
//    	// TODO Implement
//    }

    @Override
    public void run() {

        // Wait for the health check to succeed the first time
        boolean success = false;
        Exception mostRecentException = null;

        int i = 0;
        for(; i < retryCount; ++i) {
            boolean isLastIteration = i + 1 >= retryCount;

            try {
                action.run();
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
                    break;
                }
            }
        }

        if (!success) {
            throw new RuntimeException("Giving up after " + i + " failed health checks", mostRecentException);
        }
    }
}
