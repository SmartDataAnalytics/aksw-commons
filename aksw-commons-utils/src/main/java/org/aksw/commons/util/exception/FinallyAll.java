package org.aksw.commons.util.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.util.function.ThrowingRunnable;

/**
 * Force all actions in a list to run.
 *
 * Usage:
 * <pre>
 * {@code
 * FinallyAll.run(
 *   () -> action1(),
 *   () -> action2(),
 *   () -> actionN()
 * );
 * }
 * </pre>
 *
 * This is more succinct than nested finally blocks such as:
 * <pre>
 * {@code
 * try { action1(); } catch (Exception e) { throw new RuntimeException(e); } finally {
 *   try { action2(); } catch ... {
 *     try { actionN(); } catch ... {
 *     }
 *   }
 * }
 * </pre>
 *
 */
public class FinallyAll
    implements Runnable
{
    protected List<ThrowingRunnable> actions;

    public static FinallyAll create() {
        return new FinallyAll();
    }

    public FinallyAll() {
        this(new ArrayList<>());
    }

    public FinallyAll(List<ThrowingRunnable> actions) {
        super();
        this.actions = actions;
    }

    public void addThrowing(ThrowingRunnable action) {
        actions.add(action);
    }

    public void add(Callable<?> callable) {
        addThrowing(() -> { callable.call(); });
    }

    public void add(Runnable runnable) {
        addThrowing(runnable::run);
    }

    @Override
    public void run() {
        runAction(0);
    }

    protected void runAction(int index) {
        if (index < actions.size()) {
            ThrowingRunnable action = actions.get(index);
            try {
                action.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                runAction(index + 1);
            }
        }
    }

    public static void run(ThrowingRunnable ... actions) {
        new FinallyAll(Arrays.asList(actions)).run();
    }
}
