package org.aksw.commons.collections.generator;

import java.util.function.Function;

public interface Generator<T>
//extends Enumeration<Var>
{
    T next();
    T current();

    //T prefer(T item);

    /**
     * Clones should independently yield the same sequences of items as the original object
     *
     * @return
     */
    Generator<T> clone(); // throws CloneNotSupportedException;
    
    
    default <O> Generator<O> map(Function<? super T, O> fn) {
    	return new GeneratorTransforming<>(this, fn);
    }
    
    
    public static Generator<String> create(String prefix) {
    	return GeneratorFromFunction.createInt().map(value -> prefix + "_" + value);
    }
}
