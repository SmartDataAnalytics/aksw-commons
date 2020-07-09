package org.aksw.commons.collections;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Converter;

public class MutableCollectionViews {

    /**
     * Return a live-view of the given collection with conflicting elements filtered out.
     * Conflicting elements are those for which the converter raises an exception.
     *
     * @param <T>
     * @param backend
     * @param converter
     * @return
     */
    public static <T> Collection<T> filteringCollection(Collection<T> backend, Converter<? super T, ?> converter) {
        Predicate<Object> predicate = PredicateFromConverter.create(converter);
        Collection<T> result = new FilteringCollection<>(backend, predicate);
        return result;
    }

    public static <T> Set<T> filteringSet(Set<T> backend, Converter<? super T, ?> converter) {
        Predicate<Object> predicate = PredicateFromConverter.create(converter);
        Set<T> result = new FilteringSet<>(backend, predicate);
        return result;
    }


    /**
     * There is no guava Lists.filter()
     * Reason: https://stackoverflow.com/questions/8458663/guava-why-is-there-no-lists-filter-function
     *
     * @param <T>
     * @param backend
     * @param converter
     * @return
     */
    public static <T> List<T> filteringList(List<T> backend, Converter<? super T, ?> converter) {
        Predicate<Object> predicate = PredicateFromConverter.create(converter);
        List<T> result = new FilteringList<>(backend, predicate);
        return result;
    }


    public static <T, U> Collection<U> convertingCollection(Collection<T> backend, Converter<T, U> converter) {
        Collection<U> result = new ConvertingCollection<U, T, Collection<T>>(backend, converter);
        return result;
    }

    public static <T, U> Set<U> convertingSet(Set<T> backend, Converter<T, U> converter, boolean isInjective) {
        Set<U> result = new ConvertingSet<U, T, Set<T>>(backend, converter);
        return result;
    }

    public static <T, U> List<U> convertingList(List<T> backend, Converter<T, U> converter) {
        List<U> result = new ConvertingList<U, T, List<T>>(backend, converter);
        return result;
    }
}
