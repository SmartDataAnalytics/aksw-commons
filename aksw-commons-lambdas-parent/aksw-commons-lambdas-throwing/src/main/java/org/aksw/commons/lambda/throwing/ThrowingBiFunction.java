package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingBiFunction<I1, I2, O> extends Serializable {
    O apply(I1 t, I2 u) throws Exception;
}