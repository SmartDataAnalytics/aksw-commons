package org.aksw.commons.tuple.finder;

import org.aksw.commons.tuple.bridge.TupleBridge4;

public abstract class TupleFinder4Wrapper<D, C, B extends TupleFinder4<D, C>>
    extends TupleFinderWrapper<D, C, B>
    implements TupleFinder4<D, C>
{
    public TupleFinder4Wrapper(B base) {
        super(base);
    }

    @Override
    public TupleBridge4<D, C> getTupleBridge() {
        return base.getTupleBridge();
    }
}
