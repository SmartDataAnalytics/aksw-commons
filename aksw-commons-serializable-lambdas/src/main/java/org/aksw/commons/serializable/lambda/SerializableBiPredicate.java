package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.BiPredicate;

public interface SerializableBiPredicate<T, U> extends BiPredicate<T, U>, Serializable {}