package org.aksw.commons.rx.cache.range;

import java.util.Objects;

public abstract class BufferDelegateBase<A>
    implements BufferDelegate<A>
{
    @Override
    public String toString() {
        return Objects.toString(getDelegate());
    }
}
