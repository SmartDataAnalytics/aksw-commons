package org.aksw.commons.util.convert;

import java.util.function.Function;

public class ConvertFunctionImpl<I, O>
    extends ConvertFunctionBase
    implements ConvertFunction<I, O>
{
    protected Function<I, O> fn;

    public ConvertFunctionImpl(Class<?> from, Class<?> to, Function<I, O> fn) {
        super(from, to);
        this.fn = fn;
    }

    @Override
    public Function<I, O> getFunction() {
        return fn;
    }

    /** Create a converter from a lambda */
    @SuppressWarnings("unchecked")
    public static <I, O> ConvertFunction<I, O> create(
            Class<?> src,
            Class<?> tgt,
            Function<I, O> srcToTgt) {
        return new ConvertFunctionImpl<I, O>(src, tgt, in -> srcToTgt.apply((I)in));
    }
}
