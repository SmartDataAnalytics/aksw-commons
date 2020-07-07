package org.aksw.commons.accessors;

import java.util.Collection;
import java.util.function.Predicate;

import com.google.common.base.Converter;

public class ConverterUtils {
    /**
     * Return a predicate that forwards an item to the converter.convert and yields false
     * if an exception is raised.
     *
     *
     * @param <T>
     * @param converter
     * @return
     */
    public static <T> Predicate<Object> createPredicate(Converter<? super T, ?> converter) {
        return item -> {
            boolean result;
            try {
                converter.convert((T)item);
                result = true;
            } catch(Exception e) {
                result = false;
            }
            return result;
        };
    }

    /**
     * Return a live-view of the given collection with conflicting elements filtered out.
     * Conflicting elements are those for which the converter raises an exception.
     *
     * @param <T>
     * @param backend
     * @param converter
     * @return
     */
    public static <T> Collection<T> safeCollection(Collection<T> backend, Converter<? super T, ?> converter) {
        Predicate<Object> predicate = ConverterUtils.createPredicate(converter);
//        Collection<T> result = Collections2.filter(backend, predicate::test);
        Collection<T> result = new CollectionFromPredicate<>(backend, predicate::test);
        return result;
    }


// There is no guava Lists.filter() :/
// https://stackoverflow.com/questions/8458663/guava-why-is-there-no-lists-filter-function
//    public static <T> List<T> safeList(List<T> backend, Converter<? super T, ?> converter) {
//        Predicate<T> predicate = ConverterUtils.createPredicate(converter);
//        List<T> result = Lists.filter(backend, predicate::test);
//        return result;
//    }

}
