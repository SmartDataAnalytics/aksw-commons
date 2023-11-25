package org.aksw.commons.util.concurrent;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.MoreExecutors;


/**
 * Class to schedule a task after a certain delay and ensure that it only
 * executes once. If there is a request to schedule the task while it is already
 * running it will be scheduled for the delay.
 *
 * @author raven
 *
 */
public class ScheduleOnce {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleOnce.class);

    protected ScheduledExecutorService scheduledExecutorService;
    protected Duration execDelay;
    protected Callable<?> task;

    protected volatile Instant lastRequestTime = null;

    // The first request time strictly greater than lastExecTime will trigger the task
    protected volatile Instant lastExecTime = Instant.ofEpochSecond(0);
    // protected volatile Future<?> runningTask = null;

    protected final Object lock = new Object();

    public ScheduleOnce(ScheduledExecutorService scheduledExecutorService, Duration execDelay, Callable<?> task) {
        super();
        this.scheduledExecutorService = scheduledExecutorService;
        this.execDelay = execDelay;
        this.task = task;
    }

    public static ScheduleOnce scheduleOneTaskAtATime(Duration execDelay, Callable<?> task) {
        return new ScheduleOnce(
            MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(1)),
            execDelay,
            task
        );
    }

    public void scheduleTask() {
        synchronized (lock) {
            lastRequestTime = Instant.now();
            if (lastExecTime != null && lastRequestTime.isAfter(lastExecTime)) {

                if (logger.isInfoEnabled()) {
                    logger.info("Scheduled task with a delay of " + execDelay);
                }
                lastExecTime = null;
                scheduledExecutorService.schedule(() -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("Running task " + task);
                    }

                    synchronized (lock) {
                        lastExecTime = Instant.now();
                    }

                    try {
                        return task.call();
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Task execution failed", e);
                        }
                        throw new RuntimeException(e);
                    }
                }, execDelay.toMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }
}
