package org.aksw.commons.lambda.throwing;

@FunctionalInterface
public interface ThrowingBiFunction<I1, I2, O> {
	O apply(I1 t, I2 u) throws Exception;
}