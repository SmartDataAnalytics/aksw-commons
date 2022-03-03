package org.aksw.commons.index.util;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Helper interface to make transformations on streams less verbose
 *
 * Inherits from {@link Function} for out of the box chaining with {@link #andThen(Function)}.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <I>
 * @param <O>
 */
@FunctionalInterface
public interface StreamTransform<I, O>
    extends Function<Stream<I>, Stream<O>>
{
}
