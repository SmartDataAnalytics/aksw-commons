package org.aksw.commons.collector.domain;

/**
 * An accumulator produces a single output value from zero or more input values.
 *
 * @param I The input type
 * @param E The environment (aka context) type
 * @param O The output type
 */
public interface Accumulator<I, E, O> {
    /** Accumulate an input value w.r.t. to an environment */
    void accumulate(I input, E env);

    /** Convenience function that passes 'null' for the environment */
    default void accumulate(I input) {
        accumulate(input, null);
    }

    /** Obtain the output based on the so-far accumulated inputs */
    O getValue();
}
