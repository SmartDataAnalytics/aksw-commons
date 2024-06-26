package org.aksw.commons.collections.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.IteratorUtils;
import org.aksw.commons.lambda.throwing.ThrowingBiConsumer;
import org.aksw.commons.lambda.throwing.ThrowingFunction;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;

public class StreamUtils {

    /**
     * Create a single-use {@link Iterable} from a stream. Allows for easier use of Streams in
     * for-loops:
     *
     * <pre>{@code
     *   Stream<T> stream = ...;
     *   for (T item : StreamUtils.iterableOf(stream)) {
     *   }
     * }</pre>
     *
     * @param <T>
     * @param stream
     * @return
     */
    public static <T> Iterable<T> iterable(Stream<T> stream) {
        Iterable<T> result = () -> stream.iterator();
        return result;
    }


    public static <T> T expectOneItem(Stream<T> stream) {
        try {
            return IteratorUtils.expectOneItem(stream.iterator());
        } finally {
            stream.close();
        }
    }

    public static <T> T expectZeroOrOneItems(Stream<T> stream) {
        try {
            return IteratorUtils.expectZeroOrOneItems(stream.iterator());
        } finally {
            stream.close();
        }
    }

    public static <T> Stream<Entry<T, T>> streamToPairs(Stream<T> stream) {
        Iterator<T> baseIt = stream.iterator();
        Iterator<Entry<T, T>> it = new AbstractIterator<>() {
            protected T priorValue = null;
            @Override
            protected Entry<T, T> computeNext() {
                Entry<T, T> r;
                if (baseIt.hasNext()) {
                    T item = baseIt.next();
                    r = new SimpleEntry<>(priorValue, item);
                    priorValue = item;
                } else {
                    r = endOfData();
                }
                return r;
            }
        };
        Stream<Entry<T, T>> result = Streams.stream(it);
        result.onClose(() -> stream.close());
        return result;
    }

    /**
     * Note we could implement another version where each batch's List is lazy loaded from the stream -
     * but this would probably require complete consumption of each batch in order
     *
     * @param stream
     * @param batchSize
     * @return
     */
    public static <T> Stream<List<T>> mapToBatch(Stream<T> stream, int batchSize) {
        Iterator<T> baseIt = stream.iterator();
        Iterator<List<T>> it = new AbstractIterator<>() {
            @Override
            protected List<T> computeNext() {
                List<T> items = new ArrayList<>(batchSize);
                for(int i = 0; baseIt.hasNext() && i < batchSize; ++i) {
                    T item = baseIt.next();
                    items.add(item);
                }

                List<T> r = items.isEmpty()
                        ? endOfData()
                        : items;

                return r;
            }
        };
        // Iterable<List<T>> tmp = () -> it;
        Stream<List<T>> result = Streams.stream(it);
        result.onClose(() -> stream.close());
        return result;
    }

    /* Alternative implementation - but relies on an external counter - so still suboptimal
     * Best solution would be if the group key could depend on the state of the accumulator
    public static <T> Stream<List<T>> mapToBatch(Stream<T> stream, int batchSize) {
        long[] counter = {0};
        long n = batchSize;
        SequentialGroupBySpec<T, Long, List<T>> spec = SequentialGroupBySpec.create(
                (T item) -> (long)(++counter[0] / batchSize),
                () -> (List<T>)new ArrayList<T>(),
                (list, item) -> list.add(item));

        Stream<List<T>> result = StreamOperatorSequentialGroupBy.create(spec).transform(stream)).map(Entry::getValue);
        return result;
    }
    */

//    public static <T> Stream<T> stream(Iterator<T> it) {
//        Iterable<T> i = () -> it;
//        return stream(i);
//    }
//
//    public static <T> Stream<T> stream(Iterable<T> i) {
//        Stream<T> result = StreamSupport.stream(i.spliterator(), false);
//        return result;
//    }


    /**
     * Creates a new stream which upon reaching its end performs and action.
     * It concatenates the original stream with one having a single item
     * that is filtered out again. The action is run as- part of the filter.
     *
     * @param stream
     * @param runnable
     * @return
     */
    public static <T> Stream<T> appendAction(Stream<? extends T> stream, Runnable runnable) {
        Stream<T> result = Stream.concat(
                stream,
                Stream
                    .of((T)null)
                    .filter(x -> {
                        runnable.run();
                        return false;
                    })
                );
        return result;
    }

    // TODO Add to StreamUtils
    public static <S, X> Stream<X> stream(BiConsumer<S, Consumer<X>> fn, S baseSolution) {
        List<X> result = new ArrayList<>();

        fn.accept(baseSolution, (item) -> result.add(item));

        return result.stream();
    }

    /**
     * Creates a stream over a resource via an enumerable.
     * The resource is initialized lazily once the first item is read from the stream.
     * The returned stream should be used in a try-with-resources block in order
     * to close the underlying resource.
     *
     * @param <T> The record type (e.g. an array of Strings)
     * @param <R> The resource type (e.g. a java.sql.Connection)
     * @param <E> An enumerable (e.g. a java.sql.ResultSet)
     * @param resourceSupplier
     * @param toEnumerable
     * @param nextRecord
     * @param closer
     * @return
     */
    public static <T, R, E> Stream<T> fromEnumerableResource(
            Callable<R> resourceSupplier,
            ThrowingFunction<? super R, E> toEnumerable,
            ThrowingFunction<? super E, T> nextRecord,
            BiPredicate<T, ? super E> hasEnded,
            ThrowingBiConsumer<? super R, ? super E> closer) {
        IteratorOverEnumerable<T, R, E> it = new IteratorOverEnumerable<>(resourceSupplier, toEnumerable, nextRecord, hasEnded, closer);
        Stream<T> result = Streams.stream(it).onClose(it::close);
        return result;
    }

    public static class IteratorOverEnumerable<T, R, E>
        extends AbstractIterator<T>
        implements AutoCloseable
    {
        protected Callable<R> resourceSupplier;
        protected ThrowingFunction<? super R, E> toEnumerable;
        protected ThrowingFunction<? super E, T> nextRecord;
        protected BiPredicate<T, ? super E> hasEnded;
        protected ThrowingBiConsumer<? super R, ? super E> closer;

        protected boolean isClosed = false;
        protected R resource;
        protected E enumerable;

        public IteratorOverEnumerable(
            Callable<R> resourceSupplier,
            ThrowingFunction<? super R, E> toEnumerable,
            ThrowingFunction<? super E, T> nextRecord,
            BiPredicate<T, ? super E> hasEnded,
            ThrowingBiConsumer<? super R, ? super E> closer) {
            this.resourceSupplier = resourceSupplier;
            this.toEnumerable = toEnumerable;
            this.nextRecord = nextRecord;
            this.hasEnded = hasEnded;
            this.closer = closer;
        }

        @Override
        protected T computeNext() {
            if (isClosed) {
                throw new IllegalStateException("already closed");
            }

            if (enumerable == null) {
                if (resource == null) {
                    try {
                        resource = resourceSupplier.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Objects.requireNonNull(resource, "Resource supplier was expected to supply a resource but returned null.");
                    try {
                        enumerable = toEnumerable.apply(resource);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            T result;
            try {
                result = nextRecord.apply(enumerable);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            boolean isDone = hasEnded.test(result, enumerable);
            if (isDone) {
                result = endOfData();
            }
            return result;
        }

        @Override
        public void close() {
            isClosed = true;
            try {
                if (resource != null) {
                    closer.accept(resource, enumerable);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> Stream<T> viaList(Stream<T> in, Consumer<List<T>> consumer) {
        List<T> list;
        try (Stream<T> tmp = in) {
            list = tmp.collect(Collectors.toList());
        }
        if (consumer != null) {
            consumer.accept(list);
        }
        return list.stream();
    }

    public static <T> Stream<T> println(Stream<T> in) {
        return viaList(in, System.out::println);
    }
}
