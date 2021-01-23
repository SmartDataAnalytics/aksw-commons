package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface SerializableUnaryOperator<T> extends UnaryOperator<T>, Serializable {}
