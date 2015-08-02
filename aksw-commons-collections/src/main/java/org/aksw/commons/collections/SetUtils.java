package org.aksw.commons.collections;

import java.util.Set;

import com.google.common.collect.Sets;

public class SetUtils {
    public static <T> Set<T> asSet(Iterable<T> c) {
        return (c instanceof Set) ? (Set<T>) c : Sets.newHashSet(c);
    }
}
