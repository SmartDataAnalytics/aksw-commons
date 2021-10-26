package org.aksw.commons.util.ref;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

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

    protected Supplier<Ref<T>> rootRefFactory;
    protected long closeDelayInMs;

    protected transient volatile Ref<T> rawRef = null;
    protected transient volatile Ref<T> rootRef = null;

//    public LazyRef() {
//        System.out.println("ctor " + this);
//    }

    public LazyRef(Supplier<Ref<T>> rootRefFactory, long closeDelayInMs) {
        super();
//        System.out.println("init " + this);
        this.rootRefFactory = rootRefFactory;
        this.closeDelayInMs = closeDelayInMs;
    }

    public static <T> LazyRef<T> create(Supplier<Ref<T>> rootRefFactory, long delayInMs) {
        return new LazyRef<>(rootRefFactory, delayInMs);
    }

    protected transient Timer timer = null;
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