package org.aksw.commons.util.stream;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

public class StreamOperatorSequentialGroupBy<T, K, V>
    extends SequentialGroupByOperationBase<T, K, V>
{
    public static <T, K, V> StreamOperatorSequentialGroupBy<T, K, V> create(SequentialGroupBySpec<T, K, V> spec) {
        return new StreamOperatorSequentialGroupBy<>(spec);
    }

    public StreamOperatorSequentialGroupBy(SequentialGroupBySpec<T, K, V> other) {
        super(other);
    }

    /** Low-level iterator-based transformation */
    public Iterator<Entry<K, V>> transform(Iterator<T> input) {
        return new OperatorImpl(input).getDownstream();
    }

    /** Stream-based transformation. Delegates close() calls to the input stream.
     *  Relies on the input's spliterator */
    public Stream<Entry<K, V>> transform(Stream<T> input) {
        // return Streams.stream(transform(input.sequential().iterator()));
        return StreamSupport.stream(transform(input.spliterator()), input.isParallel()).onClose(input::close);
    }

    public Spliterator<Entry<K, V>> transform(Spliterator<T> upstream) {
        return new SpliteratorImpl(upstream);
    }

//    interface IteratorDelegate<T>
//        extends Iterator<T>
//    {
//        Iterator<T> getDelegate();
//
//        @Override default boolean hasNext() { return getDelegate().hasNext(); }
//        @Override default T next() { return getDelegate().next(); }
//        @Override default void remove() { getDelegate().remove(); }
//    }

    /**
     *
     * TODO This class could be turned into a spliterator as follows:
     * Upon splitting, consume one list of items with the same group key from the rhs.
     * Append this list to the spliterator of the lhs.
     *
     * @author raven
     *
     */
    public class OperatorImpl
        extends AccumulatorBase
    {
        protected Iterator<T> upstream;
        protected Iterator<Entry<K, V>> downstream;

        // The last seen item
        protected T item;

        public T getLastSeenItem() {
            return item;
        }

        public OperatorImpl(Iterator<T> upstream) {
            super();
            this.upstream = upstream;
            this.downstream = new InternalIterator();
        }

        public Iterator<Entry<K, V>> getDownstream() {
            return downstream;
        }

        public class InternalIterator extends AbstractIterator<Entry<K, V>> {

            @Override
            protected Entry<K, V> computeNext() {

                Entry<K, V> result = null;

                // It is crucial to check for a non-null result first!
                // Calling upstream.hasNext() may drain another item which
                // would then get lost in the spliterator code!
                while (result == null && upstream.hasNext()) {
                    item = upstream.next();

                    currentKey = getGroupKey.apply(item);

                    if (accNum == 0) {
                        // First time init
                        priorKey = currentKey;
                        currentAcc = accCtor.apply(accNum, currentKey);

                        ++accNum;
                    } else if (!groupKeyCompare.test(priorKey, currentKey)) {

                        result = new SimpleEntry<>(priorKey, currentAcc);

                        currentAcc = accCtor.apply(accNum, currentKey);
                        ++accNum;
                    }

                    if (currentAcc != null) {
                        currentAcc = accAdd.apply(currentAcc, item);
                    }

                    priorKey = currentKey;
                }

                // We only come here if either we have
                // a (non-null) result or input.hasNext() is false
                if (result == null) {
                    if (accNum != 0 && !lastItemSent) {
                        result = new SimpleEntry<>(currentKey, currentAcc);
                        lastItemSent = true;
                    } else {
                        result = endOfData();
                    }
                }

                return result;
            }
        }
    }



    public class SpliteratorImpl extends AbstractSpliterator<Entry<K, V>> {

        /** Aggregator used to drain one group from the rhs of a split */
        protected StreamOperatorSequentialGroupBy<T, K, List<T>> listAggregator =
            StreamOperatorSequentialGroupBy.create(SequentialGroupBySpec.createList(getGroupKey));

        protected Iterator<T> headItem;
        protected Spliterator<T> upstream;
        protected Iterator<T> tailItems;

        protected Iterator<T> iteratorView;
        protected Iterator<Entry<K, V>> aggIteratorView;


        protected SpliteratorImpl(Spliterator<T> upstream) {
            this(Collections.<T>emptyList().iterator(), upstream, Collections.<T>emptyList().iterator());
        }

        protected SpliteratorImpl(Iterator<T> headItem, Spliterator<T> upstream, Iterator<T> tailItems) {
            super(upstream.estimateSize(), Spliterator.ORDERED);
            this.headItem = headItem;
            this.upstream = upstream;
            this.tailItems = tailItems;

            updateIteratorViews();
        }

        protected void updateIteratorViews() {
            this.iteratorView = Iterators.concat(headItem, Spliterators.iterator(upstream), tailItems);
            this.aggIteratorView = new OperatorImpl(iteratorView).getDownstream();

        }

        @Override
        public Spliterator<Entry<K, V>> trySplit() {
            Spliterator<T> lhsSplit = upstream.trySplit();
            Spliterator<T> rhsSplit = upstream;

            Spliterator<Entry<K, V>> result;

            if (lhsSplit != null) {
                // Compute the new tail items from the split; for this purpose
                // aggregate one group of items from the split
                Iterator<T> rhsIt = Spliterators.iterator(rhsSplit);
                Iterator<T> rhsItPlusTail = Iterators.concat(rhsIt, tailItems);

                StreamOperatorSequentialGroupBy<T, K, List<T>>.OperatorImpl op = listAggregator.new OperatorImpl(rhsItPlusTail);
                Iterator<Entry<K, List<T>>> rhsItPlusTailIt = op.getDownstream();

                List<T> lhsTailItems = Collections.emptyList();
                // In the worst case this consumes all items from the rhs iterator
                if (rhsItPlusTailIt.hasNext()) {
                    lhsTailItems = rhsItPlusTailIt.next().getValue();
                }

                Iterator<T> rhsHeadItem;
                Iterator<T> rhsTailItems;

                // Check whether the rhs spliterator has been completely consumed
                // (togther with the tail items) using the 'lastItemSent' flag
                // We don't use rhsIt.hasNext() because that might consume another item from rhsSplit
                if (op.lastItemSent) {
                    // We have drained the rhs
                    rhsHeadItem = Collections.<T>emptyList().iterator();
                    rhsTailItems = Collections.<T>emptyList().iterator();
                } else {
                    rhsHeadItem = Collections.singletonList(op.getLastSeenItem()).iterator();
                    rhsTailItems = tailItems;
                }

                Iterator<T> lhsHeadItem = headItem;

                // Adjust the tail items of this split
                this.headItem = rhsHeadItem;
                this.upstream = rhsSplit;
                this.tailItems = rhsTailItems;
                updateIteratorViews();

                result = new SpliteratorImpl(lhsHeadItem, lhsSplit, lhsTailItems.iterator());

            } else {
                result = null;
            }

            return result;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            boolean result = aggIteratorView.hasNext();

            if (result) {
                Entry<K, V> entry = aggIteratorView.next();
                action.accept(entry);
            }

            return result;
        }
    }


    public static <T> void split(Spliterator<T> root, Consumer<? super Spliterator<T>> action) {
        Spliterator<T> rhs = root;
        Spliterator<T> lhs = root.trySplit();

        if (lhs != null) {
            System.out.println("LHS");
            action.accept(lhs);
        } else {
            System.out.println("LHS (null)");
        }

        System.out.println("RHS");
        action.accept(rhs);
    }


    public static <T> void print(Spliterator<T> root) {
        Iterator<T> it = Spliterators.iterator(root);

        it.forEachRemaining(System.out::println);
    }


    public static void main(String[] args) {

        StreamOperatorSequentialGroupBy<Integer, Integer, Integer> op =
                StreamOperatorSequentialGroupBy.create(SequentialGroupBySpec.createAcc(
                        i -> i,
                        () -> 0,
                        (acc, item) -> acc + 1));



        List<Integer> ints = Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 1, 2, 3, 4);

        // Stream<Integer> s = Stream.of(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 1, 2, 3, 4);

        // Spliterator<Integer> root = ints.spliterator();


        Spliterator<Entry<Integer, Integer>> derived = op.transform(ints.spliterator());
        // Spliterator<Entry<Integer, Integer>> derived = op.transform(ints.stream()).spliterator();

        // split(derived, StreamOperatorSequentialGroupBy::print);

        // split(derived, x -> split(x, StreamOperatorSequentialGroupBy::print));

        split(derived, x -> split(x, y -> split(y, StreamOperatorSequentialGroupBy::print)));


        if (true) {
            // return;
        }


        Stream<Integer> s = ints.stream();


        List<Entry<Integer, Integer>> actual = op.transform(s).collect(Collectors.toList());
        List<Entry<Integer, Integer>> expected = Arrays.asList(
            new SimpleEntry<>(1, 1),
            new SimpleEntry<>(2, 2),
            new SimpleEntry<>(3, 3),
            new SimpleEntry<>(4, 4),
            new SimpleEntry<>(1, 1),
            new SimpleEntry<>(2, 1),
            new SimpleEntry<>(3, 1),
            new SimpleEntry<>(4, 1)
        );

        System.out.println(expected);
        System.out.println(actual);
    }

}
