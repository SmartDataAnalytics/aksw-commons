package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.BinaryOperator;

public interface SerializableBinaryOperator<T> extends BinaryOperator<T>, Serializable {}