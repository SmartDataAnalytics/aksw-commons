package org.aksw.commons.collections;

import java.util.AbstractMap.SimpleEntry;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ListIteratorUtils {
    /**
     * Return the number of .next() invocations needed to obtain the next item.
     * A result of 0 indicates that no such item exists.
     *
     * @param <T>
     * @param listIterator
     * @param predicate
     * @return
     */
    public static <T> Entry<Integer, T> distanceToNext(
            ListIterator<T> listIterator,
            Predicate<? super T> predicate) {

        int distance = 0;
        int tmp = 0;
        T resultItem = null;
        while(listIterator.hasNext()) {
            T item = listIterator.next();
            ++tmp;

            boolean accepted = predicate.test(item);
            if(accepted) {
                resultItem = item;
                distance = tmp;
                break;
            }
        }

        // If no successor is found reset to the start position
        if(distance == 0) {
            repeatPrevious(listIterator, tmp);
        }

        return new SimpleEntry<>(distance, resultItem);
    }

    public static <T> Entry<Integer, T> distanceToPrevious(
            ListIterator<T> listIterator,
            Predicate<? super T> predicate) {

        int distance = 0;
        int tmp = 0;
        T resultItem = null;
        while(listIterator.hasPrevious()) {
            T item = listIterator.previous();
            ++tmp;

            boolean accepted = predicate.test(item);
            if(accepted) {
                resultItem = item;
                distance = tmp;
                break;
            }
        }

        // If no successor is found reset to the start position
        if(distance == 0) {
            repeatPrevious(listIterator, tmp);
        }

        return new SimpleEntry<>(distance, resultItem);
    }

    /**
     * Attempt to invoke .previous up to 'n' times and abort if .hasPrevious is false.
     * Returns the number of actual invocations.
     *
     * @param listIterator
     * @param n
     * @return
     */
    public static int repeatPrevious(ListIterator<?> listIterator, int n) {
        int result;
        for(result = 0; result < n; ++result) {
            if(!listIterator.hasPrevious()) {
                break;
            }

            // For debugging
            @SuppressWarnings("unused")
            Object skip = listIterator.previous();
        }

        return result;
    }

    public static int repeatNext(ListIterator<?> listIterator, int n) {
        int result;
        for (result = 0; result < n; ++result) {
            if (!listIterator.hasNext()) {
                break;
            }

            // For debugging
            @SuppressWarnings("unused")
            Object skip = listIterator.next();
        }

        return result;
    }

    public static <T> T getItemNext(ListIterator<T> listIterator, int n) {
        int i = 0;
        T result = null;

        for(; i < n; ++i) {
            if(!listIterator.hasNext()) {
                throw new NoSuchElementException("Request to advance " + n + " items failed after " + i + " steps");
            }

            result = listIterator.next();
        }

        return result;
    }

    public static <T> T getItemPrevious(ListIterator<T> listIterator, int n) {
        int i = 0;
        T result = null;

        for(; i < n; ++i) {
            if(!listIterator.hasPrevious()) {
                throw new NoSuchElementException("Request to advance " + n + " items failed after " + i + " steps");
            }

            result = listIterator.previous();
        }

        return result;
    }
}
