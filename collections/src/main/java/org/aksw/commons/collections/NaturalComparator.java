package org.aksw.commons.collections;

import java.util.Comparator;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:47:19 PM
 */
public class NaturalComparator<T extends Comparable<T>>
    implements Comparator<T>
{
    public int compare(T a, T b) {
        return a.compareTo(b);
    }
}
