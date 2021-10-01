package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Serializable {
    void accept(T t) throws Exception;
}