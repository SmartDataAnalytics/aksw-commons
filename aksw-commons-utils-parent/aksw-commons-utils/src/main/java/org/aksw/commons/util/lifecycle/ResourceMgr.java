package org.aksw.commons.util.lifecycle;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.commons.util.exception.FinallyRunAll.ThrowingConsumer;
import org.aksw.commons.util.function.ThrowingRunnable;

/**
 * A class where resources can be added.
 * Upon closing the resource manager, all registered resources will be freed.
 *
 * @implNote
 *   This implementation closes resources sequentially.
 */
public class ResourceMgr
    implements AutoCloseable
{
    private final Map<Object, ThrowingRunnable> resourceToCloser =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AtomicBoolean isClosed = new AtomicBoolean(false);

    public ResourceMgr() {
        super();
    }

    public <T extends AutoCloseable> T register(T closable) {
        return register(closable, AutoCloseable::close);
    }

    public <T> T register(T obj, ThrowingConsumer<? super T> closer) {
        return register(obj, () -> closer.accept(obj));
    }

    /**
     * If the resource manager has already been closed then resources are immediately closed
     * upon registration.
     */
    public <T> T register(T obj, ThrowingRunnable closeAction) {
        if (isClosed.get()) {
            try {
                closeAction.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            resourceToCloser.put(obj, closeAction);
        }
        return obj;
    }

    @Override
    public void close() {
        if (!isClosed.get()) {
            FinallyRunAll.runAll(resourceToCloser.entrySet(), e -> e.getValue().run(), null);
        }
    }

    public boolean isClosed() {
        return isClosed.get();
    }
}
