package org.aksw.commons.lambda.serializable;

import java.io.Serializable;
import java.util.function.BiPredicate;

public interface SerializableBiPredicate<T, U> extends BiPredicate<T, U>, Serializable {}