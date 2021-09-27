package org.aksw.commons.tuple;

import java.util.ArrayList;
import java.util.List;

public class TupleAccessorForList<C>
    implements TupleAccessor<List<C>, C>
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
    public <T> List<C> restore(T obj, TupleAccessorCore<? super T, ? extends C> accessor) {
        List<C> result = new ArrayList<>(dimension);
        for(int i = 0; i < dimension; ++i) {
            C item = accessor.get(obj, i);
            result.add(item);
        }
        return result;
    }
}
