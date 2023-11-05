package org.aksw.commons.util.stream;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Base class with core attributes for partial reduction of consecutive items
 * sharing some attribute (referred to as group key).
 *
 * This class is shared between specification and operation implementations.
 *
 * The most similar implementation I am aware of is
 * https://github.com/amaembo/streamex/blob/master/src/main/java/one/util/streamex/CollapseSpliterator.java
 *
 * There are minor differences in the models though:
 * - Sequential group by only creates a single accumulator per group and feeds all consecutive items to it.
 * - StreamEx seems to create multiple accumulators together with a combine function
 *
 * When accumulating consecutive triples into a graph it does not make much sense having to combine
 * individual graphs as this is quite an expensive operation due to the unnecessary indexing overhead involved.
 * (i.e. indexing of triples only to combine them afterwards anyway)
 *
 * @author raven
 *
 * @param <T> The upstream (input) item type
 * @param <K> The key type by which to group items
 * @param <V> The type of the result of grouping items
 */
public class CollapseRunsSpecBase<T, K, V> {
    /** Function to derive a group key from an item in the flow */
    protected Function<? super T, ? extends K> getGroupKey;

    /** Comparison whether two group keys are equal */
    protected BiPredicate<? super K, ? super K> groupKeyCompare;

    /** Constructor function for accumulators. Receives item index and group key */
    protected BiFunction<? super Long, ? super K, ? extends V> accCtor;

    /** Reduce an item with the accumulator to obtain a new accumulator */
    protected BiFunction<? super V, ? super T, ? extends V> accAdd;

    public CollapseRunsSpecBase(CollapseRunsSpecBase<T, K, V> other) {
        super();
        this.getGroupKey = other.getGroupKey;
        this.groupKeyCompare = other.groupKeyCompare;
        this.accCtor = other.accCtor;
        this.accAdd = other.accAdd;
    }


    public CollapseRunsSpecBase(
            Function<? super T, ? extends K> getGroupKey,
            BiPredicate<? super K, ? super K> groupKeyCompare,
            BiFunction<? super Long, ? super K, ? extends V> accCtor,
            BiFunction<? super V, ? super T, ? extends V> accAdd) {
        super();
        this.getGroupKey = getGroupKey;
        this.groupKeyCompare = groupKeyCompare;
        this.accCtor = accCtor;
        this.accAdd = accAdd;
    }
}