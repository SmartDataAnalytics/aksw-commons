package org.aksw.commons.util.memoize;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class MemoizedSupplierImpl<T>
    implements MemoizedSupplier<T>
{
    protected Supplier<T> supplier;
    protected Map<Void, T> cache;

    public MemoizedSupplierImpl(Supplier<T> supplier) {
        super();
        this.supplier = supplier;
    }


    public static <T> MemoizedSupplier<T> of(Supplier<T> supplier) {
        return new MemoizedSupplierImpl<>(supplier);
    }

    @Override
    public T get() {
        if (cache.isEmpty()) {
            synchronized (this) {
                if (cache.isEmpty()) {
                    T value = supplier.get();
                    cache = Collections.singletonMap(null, value);
                }
            }
        }

        return cache.values().iterator().next();
    }

    @Override
    public Map<Void, T> getCache() {
        return cache;
    }

    @Override
    public void clearCache() {
        cache = Collections.emptyMap();
    }
}
