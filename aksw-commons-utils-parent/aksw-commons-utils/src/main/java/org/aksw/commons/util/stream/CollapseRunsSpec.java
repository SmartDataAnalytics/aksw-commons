package org.aksw.commons.util.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Specification implementation.
 * Provides several create methods for constructing a spec about how to collapse runs.
 *
 * @author raven
 *
 * @param <T> Item type
 * @param <K> Group key type
 * @param <V> Accumulator type
 */
public class CollapseRunsSpec<T, K, V>
    extends CollapseRunsSpecBase<T, K, V>
{
    public CollapseRunsSpec(CollapseRunsSpecBase<T, K, V> other) {
        super(other);
    }

    public CollapseRunsSpec(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        super(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor is a mere supplier (and thus neither depends on the accumulator count nor the group Key)</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> CollapseRunsSpec<T, K, V> createAcc(
            Function<? super T, ? extends K> getGroupKey,
            Supplier<? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return create(getGroupKey, Objects::equals, groupKey -> accCtor.get(), accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> CollapseRunsSpec<T, K, V> createAcc(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, accAdd);
    }

    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }

    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            Supplier<? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, k -> accCtor.get(), (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }

    public static <T, K> CollapseRunsSpec<T, K, List<T>> createList(
            Function<? super T, ? extends K> getGroupKey) {
        return create(getGroupKey, () -> new ArrayList<T>(), (acc, item) -> { acc.add(item); });
    }


    /**
     * Create method with the following characteristics:
     * <ul>
     *   <li>the accumulator constructor receives the number of so-far created accumulators (starting with 0) and the group key</li>
     *   <li>Group keys are compared using Objects::equals</li>
     * </ul>
     */
    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return create(getGroupKey, Objects::equals, accCtor, (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }

    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return new CollapseRunsSpec<>(getGroupKey, groupKeyCompare, (accNum, key) -> accCtor.apply(key), accAdd);
    }


    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            Function<? super K, ? extends V> accCtor,
            BiConsumer<? super V, ? super T> accAdd) {
        return new CollapseRunsSpec<>(getGroupKey, groupKeyCompare, (accNum, key) -> accCtor.apply(key), (acc, item) -> { accAdd.accept(acc, item); return acc; });
    }


    public static <T, K, V> CollapseRunsSpec<T, K, V> create(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        return new CollapseRunsSpec<>(getGroupKey, groupKeyCompare, accCtor, accAdd);
    }
}
