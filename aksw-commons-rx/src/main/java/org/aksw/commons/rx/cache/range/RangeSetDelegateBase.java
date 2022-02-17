package org.aksw.commons.rx.cache.range;

import java.util.Objects;

public abstract class RangeSetDelegateBase<T extends Comparable<T>>
    implements RangeSetDelegate<T>
{
    @Override
    public String toString() {
        return Objects.toString(getDelegate());
    }
}
