package org.aksw.commons.tuple.finder;

import org.aksw.commons.tuple.bridge.TupleBridge3;

public abstract class TupleFinder3Wrapper<D, C, B extends TupleFinder3<D, C>>
    extends TupleFinderWrapper<D, C, B>
    implements TupleFinder3<D, C>
{
    public TupleFinder3Wrapper(B base) {
        super(base);
    }

    @Override
    public TupleBridge3<D, C> getTupleBridge() {
        return base.getTupleBridge();
    }
}
