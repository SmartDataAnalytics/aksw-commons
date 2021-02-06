package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingBinaryOperator<T> {
	T apply(T t, T u) throws Exception;
}