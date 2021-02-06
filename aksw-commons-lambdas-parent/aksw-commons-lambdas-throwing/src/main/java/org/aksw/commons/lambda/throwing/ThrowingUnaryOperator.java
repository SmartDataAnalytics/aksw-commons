package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingUnaryOperator<T> {
	T apply(T t) throws Exception;
}
