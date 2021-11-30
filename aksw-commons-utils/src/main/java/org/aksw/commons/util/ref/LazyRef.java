package org.aksw.commons.util.ref;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A supplier for a ref backed by a root ref.
 *
 * Calling .get() creates the root ref lazily if it does not yet exist.
 * Once the user ref is released, the release of the root ref is only scheduled.
 * Another request to .get() cancels a pending release.
 *
 * @author raven
 *
 * @param <T>
 */
public class LazyRef<T>
    implements Supplier<Ref<T>>, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(LazyRef.class);

    // A label used in log messages about deferred closing of the underlying resource
    protected String label;

    protected Supplier<? extends Ref<T>> backendRefSupplier;
    protected long closeDelayInMs;

    // The reference to the backing resource
    protected transient volatile Ref<T> backendRef = null;

    // Releasing the root reference starts a timer for closing the backendRef
    protected transient volatile Ref<T> rootRef = null;

    protected LazyRef() {
    }

//    public LazyRef() {
//        System.out.println("ctor " + this);
//    }

    public LazyRef(String label, Supplier<? extends Ref<T>> backendRefSupplier, long closeDelayInMs) {
        super();
//        System.out.println("init " + this);
        this.label = label;
        this.backendRefSupplier = backendRefSupplier;
        this.closeDelayInMs = closeDelayInMs;
    }

    public static <T> LazyRef<T> create(String label, Supplier<? extends Ref<T>> rootRefFactory, long delayInMs) {
        return new LazyRef<>(label, rootRefFactory, delayInMs);
    }

    protected volatile transient Timer timer = null;
    //protected transient TimerTask timerTask;

    @Override
    public Ref<T> get() {

        Ref<T> result;
        synchronized (this) {

            // If there is a scheduled close action then cancel it
            if (timer != null) {
                // System.out.println("Cancelled close");
                timer.cancel();
                timer = null;
            }

            // If there is no backend resource then get a reference to one
            if (backendRef == null) {
                backendRef = backendRefSupplier.get();
            }

            if (rootRef == null) {
                rootRef = RefImpl.create(backendRef.get(), this, () -> {

                    // As soon as the root ref is dead its field is set to null
                    this.rootRef = null;

                    if (timer == null) {

                        if (closeDelayInMs == 0) {
                            actualClose();
                        } else {
    //                        System.out.println("Scheduling close");
                            timer = new Timer();

                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    actualClose();
                                }
                            };

                            logger.debug(String.format("Deferring close of resource [%s] (backed by [%s]) by %d ms", label, backendRef, closeDelayInMs));
                            timer.schedule(timerTask, closeDelayInMs);
                        }
                    }
                });

                result = rootRef;
            }  else {
                result = rootRef.acquire();
            }

            // logger.debug("Current root ref: " + result);
        }

        return result;
    }


    protected void actualClose() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            if (backendRef != null) {
                logger.debug(String.format("Imminent close of backing resource: [%s] %s", label, backendRef));
                backendRef.close();
                backendRef = null;
            }
        }
    }


}