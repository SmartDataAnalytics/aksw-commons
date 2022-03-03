package org.aksw.commons.lambda.serializable;

import java.io.Serializable;
import java.util.function.ToIntFunction;

public interface SerializableToIntFunction<T> extends ToIntFunction<T>, Serializable {}
