package org.aksw.commons.util.stream;

public class SequentialGroupByOperationBase<T, K, V>
    extends SequentialGroupByBase<T, K, V>
{

    public SequentialGroupByOperationBase(SequentialGroupBySpec<T, K, V> other) {
        super(other);
    }

    public class AccumulatorBase
    {
        protected K priorKey;
        protected K currentKey;

        // Number of created accumulators; incremented after accCtor invocation
        protected long accNum = 0;
        protected V currentAcc = null;

        protected boolean lastItemSent = false;
    }
}