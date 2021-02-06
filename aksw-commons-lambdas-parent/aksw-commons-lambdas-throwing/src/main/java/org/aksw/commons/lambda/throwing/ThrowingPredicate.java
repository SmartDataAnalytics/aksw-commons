package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingPredicate<T> {
	boolean test(T t) throws Exception;
}