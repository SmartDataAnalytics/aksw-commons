package org.aksw.commons.util.list;

import java.util.List;

public class ListUtils {

    /**
     * Return the item at index or null if it does not exist.
     * Checks the size of the list before accessing the item (hence, the complexity of size() is ideally O(1))
     * Argument must not be negative
     *
     * @param list
     * @param i
     * @return
     */
    public static <T> T getOrNull(List<T> list, int i) {
        return getOrDefault(list, i, null);
    }

    public static <T> T getOrDefault(List<T> list, int i, T dflt) {
        T result = i >= list.size() ? dflt : list.get(i);
        return result;
    }

    /** Returns the last item of the list or null if it does not exist */
    public static <T> T lastOrNull(List<T> list) {
        return lastOrDefault(list, null);
    }

    public static <T> T lastOrDefault(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(list.size() - 1);
    }
}
