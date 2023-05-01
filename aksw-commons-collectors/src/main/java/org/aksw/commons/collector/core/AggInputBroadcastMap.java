package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggInputBroadcastMap.AccInputBroadcastMap;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;

/**
 * An aggregator that broadcasts its input to multiple sub-aggregators that accept the same input
 * and have the same structure of the output
 *
 * Serves the purpose to perform two independent type safe aggregations on the same input in a single pass.
 *
 * @author raven
 *
 * @param <I> Input type
 * @param <K> The key type by which aggregators are referred to
 * @param <O> output type common to all aggregators
 */
public class AggInputBroadcastMap<I, E, K, O>
    implements ParallelAggregator<I, E, Map<K, O>, AccInputBroadcastMap<I, E, K, O>>, Serializable
{
    private static final long serialVersionUID = 0;


    public static interface AccInputBroadcastMap<I, E, K, O>
    extends Accumulator<I, E, Map<K, O>> {
        Map<K, Accumulator<I, E, O>> getSubAccMap();
    }

    protected Map<K, ParallelAggregator<I, E, O, ?>> subAggMap;

    public AggInputBroadcastMap(Map<K, ParallelAggregator<I, E, O, ?>> subAggMap) {
        super();
        this.subAggMap = subAggMap;
    }

    @Override
    public AccInputBroadcastMap<I, E, K, O> createAccumulator() {
        Map<K, Accumulator<I, E, O>> subAccMap = subAggMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().createAccumulator()));

        return new AccInputBroadcastMapImpl(subAccMap);
    }

    @Override
    public AccInputBroadcastMap<I, E, K, O> combine(
            AccInputBroadcastMap<I, E, K, O> a,
            AccInputBroadcastMap<I, E, K, O> b) {

        Map<K, Accumulator<I, E, O>> am = a.getSubAccMap();
        Map<K, Accumulator<I, E, O>> bm = b.getSubAccMap();

        Map<K, Accumulator<I, E, O>> newMap = subAggMap.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> {
                K key = e.getKey();
                ParallelAggregator<I, E, O, ?> subAgg = e.getValue();
                Accumulator<I, E, O> as = am.get(key);
                Accumulator<I, E, O> bs = bm.get(key);
                Accumulator<I, E, O> r = subAgg.combineRaw(as, bs);
                return r;
            }));

        return new AccInputBroadcastMapImpl(newMap);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subAggMap == null) ? 0 : subAggMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AggInputBroadcastMap<?, ?, ?, ?> other = (AggInputBroadcastMap<?, ?, ?, ?>) obj;
        if (subAggMap == null) {
            if (other.subAggMap != null)
                return false;
        } else if (!subAggMap.equals(other.subAggMap))
            return false;
        return true;
    }



    public class AccInputBroadcastMapImpl
        implements AccInputBroadcastMap<I, E, K, O>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected Map<K, Accumulator<I, E, O>> keyToSubAcc;



        public AccInputBroadcastMapImpl(Map<K, Accumulator<I, E, O>> keyToSubAcc) {
            super();
            this.keyToSubAcc = keyToSubAcc;
        }

        @Override
        public void accumulate(I input, E env) {
            for (Entry<K, ParallelAggregator<I, E, O, ?>> e : subAggMap.entrySet()) {
                Accumulator<I, E, O> acc = keyToSubAcc.computeIfAbsent(e.getKey(), k -> e.getValue().createAccumulator());
                acc.accumulate(input, env);
            }
        }

        @Override
        public Map<K, O> getValue() {
            Map<K, O> result = keyToSubAcc.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue()));

            return result;
        }

        @Override
        public Map<K, Accumulator<I, E, O>> getSubAccMap() {
            return keyToSubAcc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((keyToSubAcc == null) ? 0 : keyToSubAcc.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked")
            AccInputBroadcastMapImpl other = (AccInputBroadcastMapImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (keyToSubAcc == null) {
                if (other.keyToSubAcc != null)
                    return false;
            } else if (!keyToSubAcc.equals(other.keyToSubAcc))
                return false;
            return true;
        }

        private AggInputBroadcastMap<?, ?, ?, ?> getEnclosingInstance() {
            return AggInputBroadcastMap.this;
        }
    }
}
