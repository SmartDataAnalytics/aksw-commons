package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collector.core.AggInputSplit.AccInputSplit;
import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;


/**
 * An aggregator that splits the index into a set of keys and forwards the input to the sub-aggregator's accumulator
 * for each key.
 *
 * @author raven
 *
 * @param <I>
 * @param <K>
 * @param <O>
 * @param <SUBACC>
 * @param <SUBAGG>
 */
public class AggInputSplit<I, E, K, J, O,
    SUBACC extends Accumulator<J, E, O>, SUBAGG extends ParallelAggregator<J, E, O, SUBACC>>
    implements ParallelAggregator<I, E, Map<K, O>, AccInputSplit<I, E, K, J, O, SUBACC>>,
        Serializable
{
    private static final long serialVersionUID = 0;


    public static interface AccInputSplit<I, E, K, J, O, SUBACC extends Accumulator<J, E, O>>
        extends AccWrapper<I, E, Map<K, O>, Map<K, SUBACC>> {
    }

    // A set of fixed keys
    /**  For those keys a fresh sub accumulator will be allocated immediately upon calling {@link #createAccumulator()} */
    protected Set<K> fixedKeys;

    // Whether to take into account keys besides those in the set of fixedKeys in accumulation */
    protected boolean considerNewKeys;

    // Map an input to a set of keys
    protected Function<? super I, ? extends Set<? extends K>> keyMapper;

    // Map input and key to a value
    protected BiFunction<? super I, ? super K, ? extends J> valueMapper;

    protected SUBAGG subAgg;

    /**
     * Create an instance with an empty set of fixedKeys and with addition of new keys allowed
     */
    public AggInputSplit(SUBAGG subAgg,
            Function<? super I, ? extends Set<? extends K>> keyMapper,
            BiFunction<? super I, ? super K, ? extends J> valueMapper) {
        this(subAgg, Collections.emptySet(), true, keyMapper, valueMapper);
    }

    public AggInputSplit(SUBAGG subAgg,
            Set<K> fixedKeys,
            boolean considerNewKeys,
            Function<? super I, ? extends Set<? extends K>> keyMapper,
            BiFunction<? super I, ? super K, ? extends J> valueMapper) {
        super();
        this.fixedKeys = fixedKeys;
        this.considerNewKeys = considerNewKeys;
        this.subAgg = subAgg;
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public AccInputSplit<I, E, K, J, O, SUBACC> createAccumulator() {
        Map<K, SUBACC> keyToSubAcc = new LinkedHashMap<>();
        for (K key : fixedKeys) {
            keyToSubAcc.put(key, subAgg.createAccumulator());
        }

        return new AccSplitInputImpl(keyToSubAcc);
    }

    @Override
    public AccInputSplit<I, E, K, J, O, SUBACC> combine(AccInputSplit<I, E, K, J, O, SUBACC> a,
            AccInputSplit<I, E, K, J, O, SUBACC> b) {
        Map<K, SUBACC> accA = a.getSubAcc();
        Map<K, SUBACC> accB = b.getSubAcc();


        Map<K, SUBACC> newMap = new LinkedHashMap<>();

        // Set<K> allKeys = Sets.union(accA.keySet(), accB.keySet());
        Set<K> allKeys = new HashSet<>();
        allKeys.addAll(accA.keySet());
        allKeys.addAll(accB.keySet());
        for (K key : allKeys) {
            SUBACC subAccA = accA.get(key);
            SUBACC subAccB = accB.get(key);

            SUBACC combined;
            if (subAccA != null) {
                if (subAccB != null) {
                    combined = subAgg.combine(subAccA, subAccB);
                } else {
                    combined = subAccA;
                }
            } else {
                if (subAccB != null) {
                    combined = subAccB;
                } else {
                    // Both accs are null - should never happen
                    throw new RuntimeException("Combination of two null accumulators - should never happen");
                }
            }

            newMap.put(key, combined);
        }


        return new AccSplitInputImpl(newMap);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (considerNewKeys ? 1231 : 1237);
        result = prime * result + ((fixedKeys == null) ? 0 : fixedKeys.hashCode());
        result = prime * result + ((keyMapper == null) ? 0 : keyMapper.hashCode());
        result = prime * result + ((subAgg == null) ? 0 : subAgg.hashCode());
        result = prime * result + ((valueMapper == null) ? 0 : valueMapper.hashCode());
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
        AggInputSplit<?, ?, ?, ?, ?, ?, ?> other = (AggInputSplit<?, ?, ?, ?, ?, ?, ?>) obj;
        if (considerNewKeys != other.considerNewKeys)
            return false;
        if (fixedKeys == null) {
            if (other.fixedKeys != null)
                return false;
        } else if (!fixedKeys.equals(other.fixedKeys))
            return false;
        if (keyMapper == null) {
            if (other.keyMapper != null)
                return false;
        } else if (!keyMapper.equals(other.keyMapper))
            return false;
        if (subAgg == null) {
            if (other.subAgg != null)
                return false;
        } else if (!subAgg.equals(other.subAgg))
            return false;
        if (valueMapper == null) {
            if (other.valueMapper != null)
                return false;
        } else if (!valueMapper.equals(other.valueMapper))
            return false;
        return true;
    }




    public class AccSplitInputImpl
        implements AccInputSplit<I, E, K, J, O, SUBACC>, Serializable
    {
        private static final long serialVersionUID = 0;

        protected Map<K, SUBACC> keyToSubAcc;

        public AccSplitInputImpl(Map<K, SUBACC> keyToSubAcc) {
            super();
            this.keyToSubAcc = keyToSubAcc;
        }

        @Override
        public void accumulate(I input, E env) {
            Set<? extends K> keys = keyMapper.apply(input);
            for (K key : keys) {
                if (considerNewKeys || fixedKeys.contains(key)) {
                    SUBACC subAcc = keyToSubAcc.computeIfAbsent(key, k -> subAgg.createAccumulator());

                    J newInput = valueMapper.apply(input, key);
                    subAcc.accumulate(newInput, env);
                }
            }
        }

        @Override
        public Map<K, O> getValue() {
            Map<K, O> result = keyToSubAcc.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().getValue(),
                        (u, v) -> u,
                        LinkedHashMap::new));

            return result;
        }

        @Override
        public Map<K, SUBACC> getSubAcc() {
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
            AccSplitInputImpl other = (AccSplitInputImpl) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (keyToSubAcc == null) {
                if (other.keyToSubAcc != null)
                    return false;
            } else if (!keyToSubAcc.equals(other.keyToSubAcc))
                return false;
            return true;
        }

        private AggInputSplit<?, ?, ?, ?, ?, ?, ?> getEnclosingInstance() {
            return AggInputSplit.this;
        }
    }
}
