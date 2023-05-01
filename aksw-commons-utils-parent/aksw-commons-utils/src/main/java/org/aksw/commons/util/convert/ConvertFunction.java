package org.aksw.commons.util.convert;

import java.util.function.Function;

/** A function with information about the input and output types */
public interface ConvertFunction<I, O>
    extends ConvertFunctionRaw
{
//    @Override
//    Class<I> getFrom();
//
//    @Override
//    Class<O> getTo();

    Function<I, O> getFunction();

    @Override
    default Function<Object, Object> getRawFunction() {
        return x -> getFunction().apply((I)x);
    }

    default O convert(I obj) {
        Function<I, O> fn = getFunction();
        O result = fn.apply(obj);
        return result;
    }
}
