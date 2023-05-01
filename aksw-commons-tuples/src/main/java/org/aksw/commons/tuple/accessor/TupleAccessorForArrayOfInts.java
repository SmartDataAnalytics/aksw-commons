package org.aksw.commons.tuple.accessor;

import org.aksw.commons.tuple.bridge.TupleBridge;

public class TupleAccessorForArrayOfInts
    implements TupleBridge<int[], Integer>
{
    protected int dimension;

    public TupleAccessorForArrayOfInts(int dimension) {
        super();
        this.dimension = dimension;
    }

    @Override
    public Integer get(int[] tupleLike, int componentIdx) {
        return tupleLike[componentIdx];
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public <T> int[] build(T obj, TupleAccessor<? super T, ? extends Integer> accessor) {
        int[] result = new int[dimension];
        for(int i = 0; i < dimension; ++i) {
            result[i] = accessor.get(obj, i);
        }
        return result;
    }

}
