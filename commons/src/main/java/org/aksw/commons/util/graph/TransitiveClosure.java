package org.aksw.commons.util.graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.aksw.commons.util.collections.MultiMaps;
import org.aksw.commons.util.collections.NaturalComparator;

import java.util.*;

/**
 * Created by Claus Stadler
 * Date: 2/25/11
 * Time: 12:47 PM
 */
public class TransitiveClosure {

    public static <T extends Comparable<T>> Map<T, Set<T>>transitiveClosure(Map<T, Set<T>> source)
    {
        return transitiveClosure(source, false);
    }


    public static <T> Map<T, Set<T>>transitiveClosure(Map<T, Set<T>> source, boolean inPlace)
    {
        Map<T, Set<T>> result = (inPlace == true) ? source : MultiMaps.copy(source);
        transitiveClosureInPlace(result);

        return result;
    }

    /**
     * Computes the transitive closure of a multimap using Warshall's algorithm.
     *
     * @param source
     * @param <T>
     * @return
     */
    /*
    public static <T> void transitiveClosureInPlace(Map<T, Set<T>> source, Comparator<? super T> comparator)
    {
        //NavigableSet<T> nodes = new TreeSet<T>(comparator);
        //nodes.addAll(source.keySet());
        List<T> nodes = new ArrayList<T>(source.keySet());

        for(T b : nodes) {
            for(T a : nodes) {
                if(MultiMaps.containsEntry(source, a, b)) {
                    for(T c : nodes) {
                        if(MultiMaps.containsEntry(source, b, c)) {
                            MultiMaps.put(source, a, c);
                        }
                    }
                }
            }
        }
    }
    */

    public static <T> Map<T, Set<T>> transitiveClosureInPlace(Map<T, Set<T>> source)
    {
        Map<T, Set<T>> changeSet = new HashMap<T, Set<T>>();

        do {
            for(Map.Entry<T, Set<T>> entry : source.entrySet()) {
                T a = entry.getKey();

                for(T b : entry.getValue()) {
                    for(T c : MultiMaps.safeGet(source, b)) {
                        if(!MultiMaps.containsEntry(source, a, c)) {
                            MultiMaps.put(changeSet, a, c);
                        }
                    }
                }
            }

            MultiMaps.putAll(source, changeSet);
            changeSet.clear();

        } while (!changeSet.isEmpty());

        return source;
    }

}
