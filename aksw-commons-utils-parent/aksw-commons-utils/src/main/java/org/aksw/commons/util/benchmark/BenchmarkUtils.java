package org.aksw.commons.util.benchmark;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class BenchmarkUtils {
    public static double opsPerSecByIterations(long numIterations, Callable<?> action) {
        try {
            Stopwatch sw = Stopwatch.createStarted();
            for (int i = 0; i < numIterations; ++i) {
                action.call();
            }
            long elapsed = sw.elapsed(TimeUnit.NANOSECONDS);
            double ratio = numIterations / (elapsed / 1E9);
            return ratio;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double opsPerSecByTimeLimit(Callable<?> action) {
        try {
            Stopwatch sw = Stopwatch.createStarted();
            int batchSize = 100;

            long limit = TimeUnit.NANOSECONDS.convert(Duration.ofSeconds(1));
            long elapsed = 0;
            long done = 0;
            while (elapsed < limit) {
                for (int i = 0; i < batchSize; ++i) {
                    action.call();
                }
                ++done;
                elapsed = sw.elapsed(TimeUnit.NANOSECONDS);
            }

            double ratio = (done * batchSize) / (elapsed / 1E9);
            return ratio;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
