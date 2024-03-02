package org.aksw.commons.util.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
public class FinallyRunAll
    implements Runnable
{
    protected List<ThrowingRunnable> actions;

    public static FinallyRunAll create() {
        return new FinallyRunAll();
    }

    public FinallyRunAll() {
        this(new ArrayList<>());
    }

    public FinallyRunAll(List<ThrowingRunnable> actions) {
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
        new FinallyRunAll(Arrays.asList(actions)).run();
    }
    
	public static <T> void runAll(Collection<T> actions, Consumer<T> runner, ThrowingRunnable finallyAction) {
		ThrowingRunnable[] runnables = actions.stream().map(action -> {
			ThrowingRunnable r = () -> {
				runner.accept(action);
			};
			return r;
		}).collect(Collectors.toList()).toArray(new ThrowingRunnable[0]);
		
		try {
			run(runnables);
		} finally {
			if (finallyAction != null) {
				try {
					finallyAction.run();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		// Alternative approach using a loop
		if (false) {
			List<Exception> exceptions = null;
			for (T action : actions) {
				try {
					runner.accept(action);
				} catch (Exception e) {
					if (exceptions == null) {
						exceptions = new ArrayList<>();
					}
					exceptions.add(e);
				}
			}
			
			if (exceptions != null) {
				RuntimeException e = new RuntimeException();
				exceptions.forEach(e::addSuppressed);
				throw e;
			}
		}
	}

}
