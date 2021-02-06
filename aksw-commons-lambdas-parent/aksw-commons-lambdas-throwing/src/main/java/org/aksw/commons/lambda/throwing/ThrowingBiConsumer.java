package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {
	void accept(T t, U u) throws Exception;
}