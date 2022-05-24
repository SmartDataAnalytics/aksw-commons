package org.aksw.commons.rx.op;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.aksw.commons.collector.core.Accumulators;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;


/**
 * Track all seen items of the downstream flowable. When the downstream completes,
 * invoke an action that creates a new publisher based on the seen items that
 * will be concatenated.
 *
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
public final class FlowableOperatorConditionalConcat<T, C>
    implements FlowableOperator<T, T> {

    /** The amount of items to read ahead */
    protected Aggregator<T, C> aggregator;
    protected Function<? super C, Flowable<T>> tailFlowFactory;

    public FlowableOperatorConditionalConcat(Aggregator<T, C> aggregator, Function<? super C, Flowable<T>> tailFlowFactory) {
        super();
        this.aggregator = aggregator;
        this.tailFlowFactory = tailFlowFactory;
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor is a mere supplier (and thus neither depends on the accumulator count nor the group Key)</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, C> FlowableOperatorConditionalConcat<T, C> create(
            Aggregator<T, C> aggregator,
            Function<C, Flowable<T>> tailFlowFactory) {
        return new FlowableOperatorConditionalConcat<T, C>(aggregator, tailFlowFactory);
    }


    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> downstream) throws Exception {
        return new SubscriberImpl(downstream);
    }


    /** This subscriber first consumes the initial upstream and caches all seen items.
     * Afterwards, a new flowable is created from those items and the subscriber attaches itself
     * to that new flowable. */
    public class SubscriberImpl
        implements FlowableSubscriber<T>, Subscription
    {
        protected Subscriber<? super T> downstream;
        protected Subscription upstream;
        protected boolean isInitialUpstreamComplete = false;

        protected Accumulator<T, C> accumulator;

        protected AtomicLong downstreamDemand;

        public SubscriberImpl(Subscriber<? super T> downstream) {
           this.downstream = downstream;

           this.accumulator = Accumulators.synchronize(aggregator.createAccumulator());

           this.downstreamDemand = new AtomicLong();
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (upstream != null) {
                s.cancel();
            } else {
                upstream = s;

                // Only subscribe to the downstream once
                if (!isInitialUpstreamComplete) {
                    downstream.onSubscribe(this);
                }

                long remainingDemand = downstreamDemand.get();
                if (remainingDemand != 0) {
                    upstream.request(remainingDemand);
                }
            }
        }

        @Override
        public void onNext(T item) {
            accumulator.accumulate(item);
            downstream.onNext(item);
            downstreamDemand.decrementAndGet();
        }

        /** Called when the upstream completes */
        @Override
        public void onComplete() {
            if (!isInitialUpstreamComplete) {
                isInitialUpstreamComplete = true;

                upstream = null;
                C accumulatedValue = accumulator.getValue();
                Flowable<T> tailFlow = tailFlowFactory.apply(accumulatedValue);
                if (tailFlow != null) {
                    tailFlow.subscribe(this);
                }
            } else {
                downstream.onComplete();
            }
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
        }

        @Override
        public void cancel() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }
    }
}
