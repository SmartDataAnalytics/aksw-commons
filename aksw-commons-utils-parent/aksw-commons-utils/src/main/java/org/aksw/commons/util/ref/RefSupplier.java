package org.aksw.commons.util.ref;

import java.io.Serializable;
import java.util.function.Supplier;

@FunctionalInterface
public interface RefSupplier<T>
    extends Supplier<Ref<T>>, Serializable
{
}
