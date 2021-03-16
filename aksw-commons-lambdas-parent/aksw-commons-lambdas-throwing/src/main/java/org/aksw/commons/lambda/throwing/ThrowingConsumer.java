package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingConsumer<T> {
	void accept(T t) throws Exception;
}