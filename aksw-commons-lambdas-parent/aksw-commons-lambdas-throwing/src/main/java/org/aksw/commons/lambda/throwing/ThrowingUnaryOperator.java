package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingUnaryOperator<T> extends Serializable {
    T apply(T t) throws Exception;
}
