package org.aksw.commons.rx.op;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;

/**
 * Sequential group by; somewhat similar to .toListWhile() but with dedicated support for
 * group keys and accumulators
 *
 * The constructor lambda for accumulators receives the count of so far created accumulators (starting with 0) and the group key.
 * The count can be used to 'skip' accumulation of a certain number of groups.
 *
 * The accumulator constructor function can be null - in which case the accAdd lambda is not invoked.
 * For each group key with a null accumulator a pair (groupKey, null) is emitted.
 *
 * <pre>
 * long skipCount = 5;
 * flow
 *   .lift(FlowableOperatorSequentialGroupBy.create(
 *       item -> groupOf(item),
 *       (accNum, groupKey) -> accNum < skipCount ? null : new RealAcc(),
 *       (acc, item) -> acc.add(item))
 *   .skip(skipCount)
 *   .map(Entry::getValue)
 * <pre>
 *
 *
 *
 * The items' group keys are expected to arrive in order, hence only a single accumulator is active at a time.
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
public final class FlowableOperatorSequentialGroupBy<T, K, V>
    implements FlowableOperator<Entry<K, V>, T> {

    /* Function to derive a group key from an item in the flow */
    protected Function<? super T, ? extends K> getGroupKey;

    /* Comparision whether two group keys are equal */
    protected BiPredicate<? super K, ? super K> groupKeyCompare;

    /* Constructor function for accumulators. Function argument is the group key */
    protected BiFunction<? super Long, ? super K, ? extends V> accCtor;

    /* Add an item to the accumulator */
    protected BiConsumer<? super V, ? super T> accAdd;

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor is a mere supplier (and thus neither depends on the accumulator count nor the group Key)</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Supplier<? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, groupKey -> accCtor.get(), accAdd);
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the number of so-far created accumulators (starting with 0) and the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return new FlowableOperatorSequentialGroupBy<>(getGroupKey, groupKeyCompare, (accNum, key) -> accCtor.apply(key), accAdd);
    }

    public static <T, K, V> FlowableOperatorSequentialGroupBy<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return new FlowableOperatorSequentialGroupBy<>(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    public FlowableOperatorSequentialGroupBy(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        super();
        this.getGroupKey = getGroupKey;
        this.groupKeyCompare = groupKeyCompare;
        this.accCtor = accCtor;
        this.accAdd = accAdd;
    }

    @Override
    public Subscriber<? super T> apply(Subscriber<? super Entry<K, V>> downstream) throws Exception {
        return new SubscriberImpl(downstream);
    }

    /**
     * Deprecated; Prefer using {@link Flowable#lift(FlowableOperator)} over {@link Flowable#compose(FlowableTransformer)}
     */
    @Deprecated
    public FlowableTransformer<T, Entry<K, V>> transformer() {
        return upstream -> upstream.lift(this);
    }

    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super Entry<K, V>> downstream;
        protected Subscription upstream;

        protected K priorKey;

        protected K currentKey;

        // Number of created accumulators; incremented after accCtor invocation
        protected long accNum = 0;
        protected V currentAcc = null;

        protected volatile boolean isUpstreamComplete = false;
        protected volatile boolean lastItemSent = false;

        protected AtomicLong pending = new AtomicLong();

        public SubscriberImpl(Subscriber<? super Entry<K, V>> downstream) {
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

        @Override
        public void onNext(T item) {
//            System.out.println("ONNEXT PENDING: " + pending.get() + " " + Thread.currentThread());
//            if (pending.get() <= 0) {
////                System.out.println("PENDING IS ZERO " + Thread.currentThread());
////                System.out.println("PENDING IS " + pending.get());
//                throw new RuntimeException("Received item without any pending requests");
//            }
            currentKey = getGroupKey.apply(item);

            boolean needMore = true;
            if (accNum == 0) {
                // First time init
                priorKey = currentKey;
                currentAcc = accCtor.apply(accNum, currentKey);

                // Objects.requireNonNull(currentAcc, "Got null for an accumulator");
                ++accNum;
            } else if(!groupKeyCompare.test(priorKey, currentKey)) {

                Entry<K, V> e = new SimpleEntry<>(priorKey, currentAcc);
//                System.out.println("Passing on " + e);
                needMore = pending.decrementAndGet() > 0;
                downstream.onNext(e);

                currentAcc = accCtor.apply(accNum, currentKey);
                // Objects.requireNonNull(currentAcc, "Got null for an accumulator");
                ++accNum;
            }

            if (currentAcc != null) {
                accAdd.accept(currentAcc, item);
            }

            priorKey = currentKey;

            // Whether we need more items from upstream in order to complete the current group
            if (needMore) {
                // Requesting an additional item eventually triggers either onNext() or onComplete()
                upstream.request(1);
            }
        }

        /**
         * If there is a remaining request upon onComplete then the last item can be sent out at that time.
         * Otherwise we have to wait for additional requests.
         */
        protected void trySendLastItem() {
            if (isUpstreamComplete && !lastItemSent) {

                // If we saw any items
                if (accNum != 0) {
                    if (pending.get() > 0) {
                        // System.out.println("EMITTED ITEM ON COMPLETE");
                        lastItemSent = true;
                        downstream.onNext(new SimpleEntry<>(currentKey, currentAcc));
                        downstream.onComplete();
                    }
                } else {
                    // If there were no items to send then we have no more pending last item
                    lastItemSent = true;
                    downstream.onComplete();
                }
            }
        }

        /** Called when the upstream completes */
        @Override
        public void onComplete() {
            isUpstreamComplete = true;

            trySendLastItem();
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                long before = BackpressureHelper.add(pending, n);
//                pending.addAndGet(n);
//                System.out.println("BEFORE REQUESTED " + n + " total pending " + pending.get() + " " + Thread.currentThread());


                if (before == 0) {
                    upstream.request(1);
                }
//                System.out.println("AFTER REQUESTED " + n + " total pending " + pending.get() + " " + Thread.currentThread());
            }

            trySendLastItem();
        }

        @Override
        public void cancel() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }
    }
}
