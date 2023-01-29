package org.aksw.commons.tuple.accessor;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.tuple.bridge.TupleBridge;

public class TupleAccessorForList<C>
    implements TupleBridge<List<C>, C>
{
    protected int dimension;

    public TupleAccessorForList(int dimension) {
        super();
        this.dimension = dimension;
    }

    @Override
    public C get(List<C> tupleLike, int componentIdx) {
        return tupleLike.get(componentIdx);
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public <T> List<C> build(T obj, TupleAccessor<? super T, ? extends C> accessor) {
        List<C> result = new ArrayList<>(dimension);
        for(int i = 0; i < dimension; ++i) {
            C item = accessor.get(obj, i);
            result.add(item);
        }
        return result;
    }
}
