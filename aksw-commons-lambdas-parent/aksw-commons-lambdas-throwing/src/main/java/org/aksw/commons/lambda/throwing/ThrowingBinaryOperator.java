package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBinaryOperator<T> extends Serializable {
    T apply(T t, T u) throws Exception;
}