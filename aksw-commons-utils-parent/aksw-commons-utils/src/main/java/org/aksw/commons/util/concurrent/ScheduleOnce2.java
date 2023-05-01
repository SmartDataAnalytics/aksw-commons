package org.aksw.commons.util.concurrent;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to schedule a task after a certain delay and ensure that it only
 * executes once. If there is a request to schedule the task while it is already
 * running it will be scheduled for the delay.
 *
 * @author raven
 *
 */
public class ScheduleOnce2 {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleOnce2.class);

    protected Consumer<Callable<?>> executor;
    protected Callable<?> task;


    protected volatile long lastRequestTime = 0;
    protected volatile Long lastTaskStartTime = 0l;

    protected final Object lock = new Object();

    public ScheduleOnce2(Consumer<Callable<?>> executor, Callable<?> task) {
        super();
        this.executor = executor;
        this.task = task;
    }

    public void scheduleTask() {
        synchronized (lock) {
            ++lastRequestTime;
            if (lastTaskStartTime != null && lastRequestTime > lastTaskStartTime) {
                lastTaskStartTime = null;

                logger.info("Scheduled task");
                executor.accept(() -> {
                    synchronized (lock) {
                        lastTaskStartTime = lastRequestTime;
                    }

                    logger.info("Running task " + task);

                    try {
                        return task.call();
                    } catch (Exception e) {
                        logger.warn("Task execution failed", e);
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}