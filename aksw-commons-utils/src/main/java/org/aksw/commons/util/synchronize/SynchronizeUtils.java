package org.aksw.commons.util.synchronize;

import java.util.function.Supplier;

public class SynchronizeUtils {
    public static void sync(Object synchronizer, Supplier<Boolean> condition, Runnable action) {
        Boolean val1 = condition.get();
        if (!Boolean.TRUE.equals(val1)) {
            synchronized (synchronizer) {
                Boolean val2 = condition.get();
                if (!Boolean.TRUE.equals(val2)) {
                    action.run();
                }
            }
        }
    }
}
