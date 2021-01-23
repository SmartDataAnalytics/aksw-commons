package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.Supplier;

public interface SerializableSupplier<T> extends Supplier<T>, Serializable {}