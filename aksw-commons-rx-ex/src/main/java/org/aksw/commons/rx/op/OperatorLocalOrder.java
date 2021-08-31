package org.aksw.commons.rx.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;

public final class OperatorLocalOrder<T, S>
    extends LocalOrderBase<T, S>
    implements FlowableOperator<T, T> {

    private static final Logger logger = LoggerFactory.getLogger(OperatorLocalOrder.class);

    protected S initialExpectedSeqId;

    public OperatorLocalOrder(
            S initialExpectedSeqId,
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId) {
        super(incrementSeqId, distanceFn, extractSeqId);
        this.initialExpectedSeqId = initialExpectedSeqId;
    }

    public OperatorLocalOrder(
            S initialExpectedSeqId,
            LocalOrderSpec<T, S> localOrderSpec) {
        super(localOrderSpec);
        this.initialExpectedSeqId = initialExpectedSeqId;
    }



    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super T> downstream;
        protected Subscription upstream;
        protected volatile boolean isUpstreamComplete = false;


        protected S expectedSeqId = initialExpectedSeqId;

        protected ConcurrentNavigableMap<S, T> seqIdToValue = new ConcurrentSkipListMap<>((a, b) -> distanceFn.apply(a, b).intValue());

        protected AtomicLong pending = new AtomicLong();

        public SubscriberImpl(Subscriber<? super T> downstream)
        {
           this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (upstream != null) {
                s.cancel();
            } else {
                upstream = s;
                downstream.onSubscribe(this);
            }
        }

        public void onNext(T value) {
//          if(delegate.isCancelled()) {
//              throw new RuntimeException("Downstream cancelled");
//          }

            S seqId = extractSeqId.apply(value);

//          System.err.println("ENCOUNTERED CONTRIB " + seqId + " WITH QUEUE size " + seqIdToValue.keySet().size());
            // If complete, the seqId must not be higher than the latest seen one
//            if (isUpstreamComplete) {
//                if (seqIdToValue.isEmpty()) {
//                    downstream.onError(new RuntimeException(
//                            "Sanity check failed: Call to onNext encountered after completion."));
//                }
//
//                S highestSeqId = seqIdToValue.descendingKeySet().first();
//
//                if (distanceFn.apply(seqId, highestSeqId).intValue() > 0) {
//                    downstream.onError(new RuntimeException(
//                            "Sequence was marked as complete with id " + highestSeqId
//                            + " but a higher id was encountered " + seqId));
//                }
//            }

            boolean checkForExistingKeys = true;
            if (checkForExistingKeys) {
                if (seqIdToValue.containsKey(seqId)) {
                    downstream.onError(new RuntimeException("Already seen an item with id " + seqId));
                }
            }

            // Add item to the map and drain
            drain(true, () -> seqIdToValue.put(seqId, value));
        }

        protected void drain() {
            drain(false, null);
        }

        protected void drain(boolean isCalledFromOnNext, Runnable action) {
            List<T> buffer = new ArrayList<>();
            synchronized (seqIdToValue) {
                if (action != null) {
                    action.run();
                }
                drainTo(buffer);
            }
            for (T item : buffer) {
                downstream.onNext(item);
            }

            postDrainRequests(isCalledFromOnNext);
        }

        public void postDrainRequests(boolean isCalledFromOnNext) {
            if (pending.get() > 0) {

                // If after a drain the upstream is complete and there is still
                // downstream demand then
                // trigger the onComplete event on downstream
                if (isUpstreamComplete) {
                    downstream.onComplete();

                    // Check for out-of-order items
                    if (!seqIdToValue.isEmpty()) {
                        int size = seqIdToValue.size();
                        String msg = "Upstream completed but " + size + " out of order items still queued";
                        logger.warn(msg);
                        throw new RuntimeException(msg);
                    }
                } else {
                    if (isCalledFromOnNext) {
//                        System.out.println("Requesting another item - demand is " + pending.get() + " queue size is " + seqIdToValue.size());
                        upstream.request(1);
                    }
                }
            }
        }
        protected void drainTo(Collection<T> buffer) {

            Iterator<Entry<S, T>> it = seqIdToValue.entrySet().iterator();
            while (it.hasNext() && pending.get() > 0) {
//                    if(delegate.isCancelled()) {
//                        throw new RuntimeException("Downstream cancelled");
//                    }

                Entry<S, T> e = it.next();
                S s = e.getKey();
                T v = e.getValue();

                int d = distanceFn.apply(s, expectedSeqId).intValue();
                if (d == 0) {
                    it.remove();
                    pending.decrementAndGet();
                    buffer.add(v);
                    expectedSeqId = incrementSeqId.apply(expectedSeqId);
                    // this.notifyAll();
                    // System.out.println("expecting seq id " + expectedSeqId);
                } else if (d < 0) {
                    // Skip values with a lower id
                    // TODO Add a flag to emit onError event
                    logger.warn("Should not happen: received id " + s + " which is lower than the expected id "
                            + expectedSeqId);
                    it.remove();
                } else { // if d > 0
                    // Wait for the next sequence id
                      logger.trace("Next id in queue is " + s + " but first need to wait for expected id " + expectedSeqId);
//                    System.out.println("Next id in queue is " + s + " but first need to wait for expected id " + expectedSeqId);
                    break;
                }
            }

//            System.out.println("Drain complete - pending " + pending.get() + " queue size is " + seqIdToValue.size());
        }


        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        public void onComplete() {
            isUpstreamComplete = true;

            // Calling drain after setting isUpstreamComplete to true
            // will trigger a warning / exception if there are
            // out of order items
            drain();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                long before = BackpressureHelper.add(pending, n);

                if (before == 0) {
                    upstream.request(1);
                }
            }

            // If the upstream is already complete then the request may yet
            // be served from our queue
            drain();
        }

        @Override
        public void cancel() {
            upstream.cancel();
        }
    }


    public static <T> OperatorLocalOrder<T, Long> forLong(long initiallyExpectedId, Function<? super T, ? extends Long> extractSeqId) {
        return new OperatorLocalOrder<T, Long>(
                initiallyExpectedId,
                id -> Long.valueOf(id.longValue() + 1l),
                (a, b) -> a - b, extractSeqId);
    }

    public static <T, S extends Comparable<S>> OperatorLocalOrder<T, S> wrap(S initiallyExpectedId, Function<? super S, ? extends S> incrementSeqId, BiFunction<? super S, ? super S, ? extends Number> distanceFn, Function<? super T, ? extends S> extractSeqId) {
        return new OperatorLocalOrder<T, S>(initiallyExpectedId, incrementSeqId, distanceFn, extractSeqId);
    }

    public static <T, S extends Comparable<S>> FlowableOperator<T, T> create(
            S initialExpectedSeqId,
            LocalOrderSpec<T, S> orderSpec) {
        return new OperatorLocalOrder<T, S>(initialExpectedSeqId, orderSpec);
    }


    public static <T, S extends Comparable<S>> FlowableOperator<T, T> create(
            S initialExpectedSeqId,
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId) {
        return new OperatorLocalOrder<T, S>(initialExpectedSeqId, incrementSeqId, distanceFn, extractSeqId);
    }

    @Override
    public @NonNull Subscriber<? super @NonNull T> apply(@NonNull Subscriber<? super @NonNull T> downstream)
            throws Throwable {
        return new SubscriberImpl(downstream);
    }
}