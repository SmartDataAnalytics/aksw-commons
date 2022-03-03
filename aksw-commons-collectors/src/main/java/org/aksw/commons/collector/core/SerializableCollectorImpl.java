package org.aksw.commons.collector.core;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.lambda.serializable.SerializableBiConsumer;
import org.aksw.commons.lambda.serializable.SerializableBinaryOperator;
import org.aksw.commons.lambda.serializable.SerializableCollector;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;

/**
 * A collector implementation that implements Serializable.
 * Note that the constructor does NOT mandate the use of serializable versions of the lambdas.
 * In order to create an instance from lambdas that are made serializable
 * use the method {@link #create(SerializableSupplier, SerializableBiConsumer, SerializableBinaryOperator, SerializableFunction)}
 *
 *
 * @author raven
 *
 * @param <T>
 * @param <A>
 * @param <R>
 */
public class SerializableCollectorImpl<T, A, R>
    implements SerializableCollector<T, A, R>
    // implements Collector<T, A, R>, Serializable
{
    private static final long serialVersionUID = 448920416560172402L;

    protected Supplier<A> supplier;
    protected BiConsumer<A, T> accumulator;
    protected BinaryOperator<A> combiner;
    protected Function<A, R> finisher;
    protected Set<Characteristics> characteristics;

    public SerializableCollectorImpl(
            Supplier<A> supplier,
            BiConsumer<A, T> accumulator,
            BinaryOperator<A> combiner,
            Function<A, R> finisher,
            Set<Characteristics> characteristics) {
        super();
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    @Override public BiConsumer<A, T> accumulator() { return accumulator; }

    @Override public Set<Characteristics> characteristics() { return characteristics; }
    @Override public BinaryOperator<A> combiner() { return combiner; }
    @Override public Function<A, R> finisher() { return finisher; }
    @Override public Supplier<A> supplier() { return supplier; }

    public static <T, A, R> SerializableCollectorImpl<T, A, R> create(
            SerializableSupplier<A> supplier,
            SerializableBiConsumer<A, T> accumulator,
            SerializableBinaryOperator<A> combiner,
            SerializableFunction<A, R> finisher) {
        return create(supplier, accumulator, combiner, finisher, Collections.emptySet());
    }

    public static <T, A, R> SerializableCollectorImpl<T, A, R> create(
            SerializableSupplier<A> supplier,
            SerializableBiConsumer<A, T> accumulator,
            SerializableBinaryOperator<A> combiner,
            SerializableFunction<A, R> finisher,
            Set<Characteristics> characteristics) {
        return new SerializableCollectorImpl<>(supplier, accumulator, combiner, finisher, characteristics);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accumulator == null) ? 0 : accumulator.hashCode());
        result = prime * result + ((characteristics == null) ? 0 : characteristics.hashCode());
        result = prime * result + ((combiner == null) ? 0 : combiner.hashCode());
        result = prime * result + ((finisher == null) ? 0 : finisher.hashCode());
        result = prime * result + ((supplier == null) ? 0 : supplier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SerializableCollectorImpl<?, ?, ?> other = (SerializableCollectorImpl<?, ?, ?>) obj;
        if (accumulator == null) {
            if (other.accumulator != null)
                return false;
        } else if (!accumulator.equals(other.accumulator))
            return false;
        if (characteristics == null) {
            if (other.characteristics != null)
                return false;
        } else if (!characteristics.equals(other.characteristics))
            return false;
        if (combiner == null) {
            if (other.combiner != null)
                return false;
        } else if (!combiner.equals(other.combiner))
            return false;
        if (finisher == null) {
            if (other.finisher != null)
                return false;
        } else if (!finisher.equals(other.finisher))
            return false;
        if (supplier == null) {
            if (other.supplier != null)
                return false;
        } else if (!supplier.equals(other.supplier))
            return false;
        return true;
    }

};
