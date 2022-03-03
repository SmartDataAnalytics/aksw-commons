package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBiPredicate<T, U> extends Serializable {
    boolean test(T t, U u) throws Exception;
}