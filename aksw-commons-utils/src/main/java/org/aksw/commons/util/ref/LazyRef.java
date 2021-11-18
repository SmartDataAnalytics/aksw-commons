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

    protected Supplier<? extends Ref<T>> rootRefFactory;
    protected long closeDelayInMs;

    protected transient volatile Ref<T> rawRef = null;
    protected transient volatile Ref<T> rootRef = null;

    protected LazyRef() {
    }

//    public LazyRef() {
//        System.out.println("ctor " + this);
//    }

    public LazyRef(String label, Supplier<? extends Ref<T>> rootRefFactory, long closeDelayInMs) {
        super();
//        System.out.println("init " + this);
        this.label = label;
        this.rootRefFactory = rootRefFactory;
        this.closeDelayInMs = closeDelayInMs;
    }

    public static <T> LazyRef<T> create(String label, Supplier<? extends Ref<T>> rootRefFactory, long delayInMs) {
        return new LazyRef<>(label, rootRefFactory, delayInMs);
    }

    protected volatile transient Timer timer = null;
    //protected transient TimerTask timerTask;

    @Override
    public Ref<T> get() {
//        System.out.println("get " + this);
        Ref<T> result;
        synchronized (this) {
            if (timer != null) {
//                System.out.println("Cancelled close");
                timer.cancel();
                timer = null;
            }

            if (rawRef == null) {
                rawRef = rootRefFactory.get();
            }

            if (rootRef == null || !rootRef.isAlive()) {
                rootRef = RefImpl.create(rawRef.get(), this, () -> {
                    if (timer == null) {
                        Runnable closeAction = () -> {
                            synchronized (this) {
                                timer.cancel();
                                timer = null;

                                logger.debug(String.format("Closing resource [%s]", label));
                                rawRef.close();
                                rawRef = null;
                                rootRef = null;
                            }
                        };

                        if (closeDelayInMs == 0) {
                            closeAction.run();
                        } else {
    //                        System.out.println("Scheduling close");
                            timer = new Timer();

                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    closeAction.run();
                                }
                            };

                            logger.debug(String.format("Deferring close of resource [%s] by " + closeDelayInMs + " ms", label));
                            timer.schedule(timerTask, closeDelayInMs);
                        }
                    }
                });

                result = rootRef;
            }  else {
                result = rootRef.acquire();
            }
        }

        return result;
    }


}