package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingSupplier<T> extends Serializable {
    T get() throws Exception;
}