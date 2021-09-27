package org.aksw.commons.index.util;

import java.util.Set;

public interface CSet<T, X>
    extends Set<T>, HasData<X>
{
    @Override
    CSet<T, X> setData(X data);
}
