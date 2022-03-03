package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> extends Serializable {
    void accept(T t, U u) throws Exception;
}