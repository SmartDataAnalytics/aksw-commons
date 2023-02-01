package org.aksw.commons.tuple.util;

import java.util.Comparator;
import java.util.function.BiPredicate;

import org.aksw.commons.tuple.accessor.TupleAccessor;

public class TupleCmp {

    public static <D1, D2, C> int compare(
            int n,
            Comparator<C> cmp,
            D1 d1, TupleAccessor<D1, ? extends C> d1a,
            D2 d2, TupleAccessor<D2, ? extends C> d2a) {
        int result = 0;
        for (int i = 0; i < n; ++i) {
            C c1 = d1a.get(d1, i);
            C c2 = d2a.get(d2, i);
            int d = cmp.compare(c1, c2);
            if (d != 0) {
                result = d;
                break;
            }
        }
        return result;
    }

    public static <D1, D2, C> boolean matches(
            int n,
            BiPredicate<C, C> cmp,
            D1 d1, TupleAccessor<D1, ? extends C> d1a,
            D2 d2, TupleAccessor<D2, ? extends C> d2a) {
        boolean result = true;
        for (int i = 0; i < n; ++i) {
            C c1 = d1a.get(d1, i);
            C c2 = d2a.get(d2, i);
            boolean isMatch = cmp.test(c1, c2);
            if (!isMatch) {
                result = false;
                break;
            }
        }
        return result;
    }

}
