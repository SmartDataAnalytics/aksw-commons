package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingSupplier<T> {
	T get() throws Exception;
}