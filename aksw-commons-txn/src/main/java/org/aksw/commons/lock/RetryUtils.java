package org.aksw.commons.lock;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);

    public static <T> T simpleRetry(
            long retryCount,
            long delayInMs,
            Callable<T> action) {

        T result = null;
        long retryAttempt ;
        for (retryAttempt = 0; retryAttempt < retryCount; ++retryAttempt) {
            try {
                result = action.call();
                logger.trace(String.format("Retry attempt %d/%d succeeded", retryAttempt, retryCount));
                break;
            } catch (Exception e) {
                // logger.warn("Retry failed: " + ExceptionUtils.getRootCauseMessage(e));
                logger.trace(String.format("Retry attempt %d/%d failed: ", retryAttempt + 1, retryCount) + ExceptionUtils.getRootCauseMessage(e), e);
                if (retryAttempt + 1 == retryCount) {
                    throw new RuntimeException(e);
                } else {
                    try {
                        Thread.sleep(delayInMs);
                    } catch (Exception e2) {
                        throw new RuntimeException(e2);
                    }
                }
            }
        }
        return result;
    }
}
