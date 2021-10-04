package org.aksw.commons.collection.observable;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamOps {
    public static <T> Collection<T> collect(boolean duplicateAware, Stream<? extends T> stream) {
        return duplicateAware
                ? stream.collect(Collectors.toList())
                : stream.collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> filter(Stream<?> in, Predicate<? super T> predicate) {
        return in.filter(x -> {
            boolean r;
            try {
                r = predicate.test((T)x);
            } catch (ClassCastException e) {
                r = false;
            }
            return r;
        })
        .map(x -> (T)x);
    }
}
