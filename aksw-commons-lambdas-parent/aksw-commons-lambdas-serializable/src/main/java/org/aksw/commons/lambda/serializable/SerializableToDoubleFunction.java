package org.aksw.commons.lambda.serializable;

import java.io.Serializable;
import java.util.function.ToDoubleFunction;

public interface SerializableToDoubleFunction<T> extends ToDoubleFunction<T>, Serializable {}
