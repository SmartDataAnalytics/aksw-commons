package org.aksw.commons.util.memoize;

import java.util.function.Supplier;

public interface MemoizedSupplier<T>
    extends Supplier<T>, Memoized<Void, T>
{

}
