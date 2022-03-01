package org.aksw.commons.io.buffer.plain;

import java.util.Objects;

public abstract class BufferDelegateBase<A>
    implements BufferDelegate<A>
{
    @Override
    public String toString() {
        return Objects.toString(getDelegate());
    }
}
