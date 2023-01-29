package org.aksw.commons.tuple.finder;

import java.util.Objects;

import org.aksw.commons.tuple.bridge.TupleBridge;

public abstract class TupleFinderWrapper<D, C, B extends TupleFinder<D, C>>
    implements TupleFinder<D, C>
{
    protected B base;

    public TupleFinderWrapper(B base) {
        super();
        this.base = Objects.requireNonNull(base);
    }

    @Override
    public TupleBridge<D, C> getTupleBridge() {
        return base.getTupleBridge();
    }
}
