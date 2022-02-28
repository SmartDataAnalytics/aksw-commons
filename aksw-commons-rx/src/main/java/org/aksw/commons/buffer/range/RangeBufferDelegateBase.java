package org.aksw.commons.buffer.range;

import java.util.Objects;

/** This class really only exists for the toString method */
public abstract class RangeBufferDelegateBase<A>
    implements RangeBufferDelegate<A>
{

    @Override
    public String toString() {
        return Objects.toString(getDelegate());
    }
}
