package org.aksw.commons.collections.generator;

import java.util.function.Predicate;

public class GeneratorFilter<T>
    extends GeneratorForwarding<T>
{
    protected Predicate<? super T> predicate;

    public GeneratorFilter(Generator<T> delegate, Predicate<? super T> predicate) {
        super(delegate);
        this.predicate = predicate;
    }

    @Override
    public GeneratorForwarding<T> clone() {
        return new GeneratorFilter<>(getDelegate().clone(), predicate);
    }

    @Override
    public T next() {
        T result;
        while (!predicate.test((result = super.next()))) {
        }
        return result;
    }
}
