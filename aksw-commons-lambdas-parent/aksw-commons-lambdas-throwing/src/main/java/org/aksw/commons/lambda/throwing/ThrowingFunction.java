package org.aksw.commons.lambda.throwing;

import java.io.Serializable;

@FunctionalInterface
public interface ThrowingFunction<I, O> extends Serializable {
    O apply(I t) throws Exception;
}
