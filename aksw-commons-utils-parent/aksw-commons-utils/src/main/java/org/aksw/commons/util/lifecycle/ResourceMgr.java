package org.aksw.commons.util.lifecycle;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

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

    public ResourceMgr() {
        super();
    }

    public <T extends AutoCloseable> T register(T closable) {
        return register(closable, AutoCloseable::close);
    }

    public <T> T register(T obj, ThrowingConsumer<? super T> closer) {
        return register(obj, () -> closer.accept(obj));
    }

    public <T> T register(T obj, ThrowingRunnable closeAction) {
        resourceToCloser.put(obj, closeAction);
        return obj;
    }

    @Override
    public void close() {
        FinallyRunAll.runAll(resourceToCloser.entrySet(), e -> e.getValue().run(), null);
    }
}
