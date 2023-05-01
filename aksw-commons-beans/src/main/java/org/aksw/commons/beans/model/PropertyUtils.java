package org.aksw.commons.beans.model;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.lambda.throwing.ThrowingConsumer;
import org.aksw.commons.lambda.throwing.ThrowingFunction;
import org.aksw.commons.lambda.throwing.ThrowingSupplier;

/**
 * Utility methods for applying defaults to properties
 *
 * @author Claus Stadler
 *
 */
public class PropertyUtils {

    /**
     * Invoke the setter with the getter's value - unless the getter returns null.
     *
     * @param setter
     * @param getter
     * @return
     */
    public static <T> T applyIfPresent(ThrowingConsumer<? super T> setter, ThrowingSupplier<? extends T> getter) {
        try {
            T result = getter.get();
            if (result != null) {
                setter.accept(result);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T applyIfPresent(ThrowingFunction<? super T, ?> setter, ThrowingSupplier<? extends T> getter) {
        try {
            T result = getter.get();
            if (result != null) {
                setter.apply(result);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If targetGetter yields null, invoke the targetSetter with the valueGetter's result.
     *
     * @param targetSetter
     * @param targetGetter
     * @param valueGetter
     * @return
     */
    public static <T> T applyIfAbsent(Consumer<? super T> targetSetter, Supplier<? extends T> targetGetter, Supplier<? extends T> valueGetter) {
        T result = targetGetter.get();
        if(result == null) {
            result = valueGetter.get();
            targetSetter.accept(result);
        }

        return result;
    }

    /**
     * If targetGetter yields null, invoke the targetSetter with the valueGetter's result.
     *
     * @param targetSetter
     * @param targetGetter
     * @param valueGetter
     * @return
     */
    public static <T> T applyIfAbsent(Function<? super T, ?> targetSetter, Supplier<? extends T> targetGetter, Supplier<? extends T> valueGetter) {
        T result = targetGetter.get();
        if(result == null) {
            result = valueGetter.get();
            targetSetter.apply(result);
        }

        return result;
    }

}
