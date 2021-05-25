package org.aksw.commons.collections.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.aksw.commons.collections.IteratorUtils;

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

        Iterator<List<T>> it = new AbstractIterator<List<T>>() {
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

        Iterable<List<T>> tmp = () -> it;
        Stream<List<T>> result = Streams.stream(tmp);
        result.onClose(() -> stream.close());
        return result;
    }

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

}
