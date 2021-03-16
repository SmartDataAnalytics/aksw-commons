package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingFunction<I, O> {
	O apply(I t) throws Exception;
}
