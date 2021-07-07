package org.aksw.commons.rx.op;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;


/**
 *
 * <pre>{@code
 * 		Flowable<Entry<Integer, List<Integer>>> list = Flowable
 *			.range(0, 10)
 *			.map(i -> Maps.immutableEntry((int)(i / 3), i))
 *			.lift(FlowableOperatorSequentialGroupBy.<Entry<Integer, Integer>, Integer, List<Integer>>create(Entry::getKey, ArrayList::new, (acc, e) -> acc.add(e.getValue())));
 *
 * }</pre>
 *
 * @author raven
 *
 * @param <T> Item type
 * @param <K> Group key type
 * @param <V> Accumulator type
 */
public final class FlowableOperatorReadAhead<T>
    implements FlowableOperator<T, T> {

    /** The amount of items to read ahead */
    protected int readAheadAmount;

    public FlowableOperatorReadAhead(int readAheadAmount) {
        super();
        this.readAheadAmount = readAheadAmount;
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor is a mere supplier (and thus neither depends on the accumulator count nor the group Key)</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T> FlowableOperatorReadAhead<T> create(int readAheadAmount) {
        return new FlowableOperatorReadAhead<T>(readAheadAmount);
    }


    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> downstream) throws Exception {
        return new SubscriberImpl(downstream);
    }


    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super T> downstream;
        protected Subscription upstream;
        protected boolean isUpstreamCompleted = false;

        protected Deque<T> queue;
        protected AtomicLong downstreamDemand;
        protected AtomicInteger readAheadCapacity;

        public SubscriberImpl(Subscriber<? super T> downstream) {
           this.downstream = downstream;
           this.queue = new ArrayDeque<>(readAheadAmount + 1);
           this.downstreamDemand = new AtomicLong();
           this.readAheadCapacity= new AtomicInteger(readAheadAmount + 1);
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

        /** From the queue send out as many items as requested */
        protected void drain() {
            int n = 0;


            // getAndUpdate: Decrement by 1 unless the value is already 0
            while (!queue.isEmpty() && downstreamDemand.getAndUpdate(v -> v == 0 ? 0 : v - 1) > 0) {
                T item = null;
                try {
                    item = queue.removeFirst();
                } catch (NoSuchElementException e) {
                    // The queue concurrently became empty - just leave the item null
                }

                if (item != null) {
                    downstream.onNext(item);
                    ++n;
                } else {
                    // There was no item to send - so increase the demand again
                    downstreamDemand.addAndGet(1);
                    break;
                }
            }


            if (isUpstreamCompleted) {
                if (queue.isEmpty()) {
                    downstream.onComplete();
                }
            } else {
                if (n != 0) {
                    readAheadCapacity.addAndGet(n);
                }

                int capacity = readAheadCapacity.getAndSet(0);
                if (capacity != 0) {
                    upstream.request(capacity);
                }
            }
        }

        @Override
        public void onNext(T item) {
            // Unconditionally adding items to the queue might be
            // Implicitly over performance
            queue.add(item);
            drain();
        }

        /** Called when the upstream completes */
        @Override
        public void onComplete() {
            isUpstreamCompleted = true;
            drain();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(downstreamDemand, n);
            }

            drain();
        }

        @Override
        public void cancel() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }
    }
}
