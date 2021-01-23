package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.Predicate;

public interface SerializablePredicate<T> extends Predicate<T>, Serializable {}