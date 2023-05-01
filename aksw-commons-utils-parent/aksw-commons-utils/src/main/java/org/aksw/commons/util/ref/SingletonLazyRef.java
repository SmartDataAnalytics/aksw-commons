package org.aksw.commons.util.ref;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.aksw.commons.util.obj.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper that turns a supplier of references into a singleton such that
 * at any given time (on a specific jvm) only a most a single reference will have been
 * acquired from that supplier.
 *
 * The intended use case is to allow for sharing a supplier of a {@link javax.sql.DataSource}
 * across a cluster. Each involved jvm may obtain its own singleton instnance from that supplier
 * such that there is only one connection pool per jvm from where connections can be drawn.
 *
 */
public class SingletonLazyRef<T>
    implements Supplier<Ref<T>>, Serializable
{
    private static final Logger logger = LoggerFactory.getLogger(SingletonLazyRef.class);

    private static final long serialVersionUID = 1L;

    private static Map<String, Ref<?>> activeSingletons = new ConcurrentHashMap<>();


    protected String instanceId;
    protected Supplier<? extends Ref<T>> refSupplier;

    public SingletonLazyRef(String instanceId, Supplier<? extends Ref<T>> refSupplier) {
        super();
        this.instanceId = instanceId;
        this.refSupplier = refSupplier;
    }

    public static <T> SingletonLazyRef<T> create(String id, Supplier<? extends Ref<T>> refSupp) {
        return new SingletonLazyRef<>(id, refSupp);
    }

    public static <T> SingletonLazyRef<T> create(Supplier<? extends Ref<T>> refSupp) {
        String id = Instant.now() + "_" + System.identityHashCode(refSupp) +"_" + refSupp.toString();
        return create(id, refSupp);
    }

    /**
     * Every call to .get() returns a fresh reference that must eventually be released!
     *
     */
    @Override
    public Ref<T> get() {
        Ref<T> result;
        synchronized (activeSingletons) {
            @SuppressWarnings("unchecked")
            Ref<T> activeRef = (Ref<T>)activeSingletons.get(instanceId);

            if (activeRef == null) {
                Ref<T> delegateRef = refSupplier.get();

                logger.debug("Acquired singleton delegate " + ObjectUtils.toStringWithIdentityHashCode(delegateRef));

                activeRef = RefImpl.create(delegateRef.get(), activeSingletons, () -> {
                    delegateRef.close();
                    logger.debug("Closed singleton delegate " + ObjectUtils.toStringWithIdentityHashCode(delegateRef));

                    activeSingletons.remove(instanceId);

                    // assert !activeSingletons.containsKey(instanceId);
                });

                // There can be multiple SingletonLazyRef instances that share the same instanceId
                // and refSupplier. Only one delegate instance is ever created though
                activeSingletons.put(instanceId, activeRef);

                result = activeRef;
            } else {
                result = activeRef.acquire();
            }
        }
        return result;
    }
}
