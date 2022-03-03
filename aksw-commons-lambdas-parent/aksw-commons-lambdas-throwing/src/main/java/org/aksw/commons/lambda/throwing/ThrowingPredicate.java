package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingPredicate<T> extends Serializable {
    boolean test(T t) throws Exception;
}