package org.aksw.commons.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/22/11
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapUtils {

    public static <T> int addWithIntegerAutoIncrement(Map<T, Integer> map, T key) {
        int result = map.computeIfAbsent(key, (k) -> map.size());
        return result;
    }

    public static <T> long addWithLongAutoIncrement(Map<T, Long> map, T key) {
        long result = map.computeIfAbsent(key, (k) -> (long)map.size());
        return result;
    }

    public static void removeAll(Map<?, ?> map, Iterable<?> items) {
        for(Object o : items) {
            map.remove(o);
        }
    }

    /**
     * Set the same value for a given set of keys
     *
     * @param map
     * @param keys
     * @param value
     */
    public static <K, V> void putForAll(Map<K, V> map, Iterable<K> keys, V value) {
        for(K key : keys) {
            map.put(key, value);
        }
    }


    /**
     * Compatible means that merging the two maps would not result in the same
     * key being mapped to distinct values.
     *
     * Or put differently:
     * The union of the two maps retains a functional mapping.
     *
     * @param <K>
     * @param <V>
     * @param a
     * @param b
     * @return
     */
    public static <K, V> boolean isCompatible(Map<? extends K, ? extends V> a, Map<? extends K, ? extends V> b) {
        Set<? extends K> commonKeys = Sets.intersection(a.keySet(), b.keySet());
        boolean result = isCompatible(commonKeys, a, b);
        return result;
    }

    public static <K, V> boolean isCompatible(Set<? extends K> keysToTest, Map<? extends K, ? extends V> a, Map<? extends K, ? extends V> b) {
        boolean result = true;
        for(K key : keysToTest) {
            V av = a.get(key);
            V bv = b.get(key);
            result = Objects.equal(av, bv);
            if(!result) {
                break;
            }
        }

        return result;
    }

    // A version written before guava - Sets.intersection can make sure that any tested key is actually contained in the keyset.
    @Deprecated
    public static <K, V> boolean isPartiallyCompatible(Map<K, V> a, Map<K, V> b) {
        boolean result = isCompatible(a, b);
        return result;
    }

    public static <K, V> Multimap<V, K> reverse(Map<K, V> map) {
        Multimap<V, K> result = HashMultimap.create();

        for(Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }

        return result;
    }

    public static <K, V> V getOrElse(Map<? extends K, ? extends V> map, K key, V elze)
    {
        if(map.containsKey(key)) {
            return map.get(key);
        }

        return elze;
    }

      public static <K, V> Map<K, V> createChainMap(Map<K, ?> a, Map<?, V> b) {
        Map<K, V> result = new HashMap<K, V>();

        for(Map.Entry<K, ?> entry : a.entrySet()) {
            if(b.containsKey(entry.getValue())) {
                result.put(entry.getKey(), b.get(entry.getValue()));
            }
        }

        return result;
    }

    public static <K, V> V getOrCreate(Map<K, V> map, K key, Class<V> clazz, Object ... ctorArgs)
    {
        V result = map.get(key);
        if(result == null) {
            // TODO Invoke the correct constructor based on the arguments
            //Class[] classes = new Class[ctorArgs.length];
            //clazz.getConstructor();


            if(ctorArgs.length > 0) {
                throw new RuntimeException("Constructor arguments not supported yet");
            } else  {
                try {
                    result = (V)clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            map.put(key, result);
        }

        return result;
    }

    /**
         * Find a mapping of variables from cand to query, such that the pattern of
         * cand becomes a subset of that of query
         *
         * null if no mapping can be established
         *
         * @param query
         * @param cand
         * @return
         */
    //
    //    public Iterator<Map<Var, Var>> computeVarMapQuadBased(PatternSummary query, PatternSummary cand, Set<Set<Var>> candVarCombos) {
    //
    //        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToCandQuad = cand.getQuadToCnf().getInverse();
    //        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToQueryQuad = query.getQuadToCnf().getInverse();
    //
    //        //IBiSetMultimap<Quad, Quad> candToQuery = new BiHashMultimap<Quad, Quad>();
    ////        Map<Set<Set<Expr>>, QuadGroup> cnfToQuadGroup = new HashMap<Set<Set<Expr>>, QuadGroup>();
    //        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>();
    //        for(Entry<Set<Set<Expr>>, Collection<Quad>> entry : cnfToCandQuad.asMap().entrySet()) {
    //
    //            //Quad candQuad = entry.getKey();
    //            Set<Set<Expr>> cnf = entry.getKey();
    //
    //            Collection<Quad> candQuads = entry.getValue();
    //            Collection<Quad> queryQuads = cnfToQueryQuad.get(cnf);
    //
    //            if(queryQuads.isEmpty()) {
    //                return Collections.<Map<Var, Var>>emptySet().iterator();
    //            }
    //
    //            QuadGroup quadGroup = new QuadGroup(candQuads, queryQuads);
    //            quadGroups.add(quadGroup);
    //
    //            // TODO We now have grouped together quad having the same constraint summary
    //            // Can we derive some additional constraints form the var occurrences?
    //
    //
    ////            SetMultimap<Quad, Quad> summaryToQuadsCand = quadJoinSummary(new ArrayList<Quad>(candQuads));
    ////            System.out.println("JoinSummaryCand: " + summaryToQuadsCand);
    ////
    ////            SetMultimap<Quad, Quad> summaryToQuadsQuery = quadJoinSummary(new ArrayList<Quad>(queryQuads));
    ////            System.out.println("JoinSummaryQuery: " + summaryToQuadsQuery);
    ////
    ////            for(Entry<Quad, Collection<Quad>> candEntry : summaryToQuadsCand.asMap().entrySet()) {
    ////                queryQuads = summaryToQuadsQuery.get(candEntry.getKey());
    ////
    ////                // TODO What if the mapping is empty?
    ////                QuadGroup group = new QuadGroup(candEntry.getValue(), queryQuads);
    ////
    ////                cnfToQuadGroup.put(cnf, group);
    ////            }
    //        }
    //
    //        // Figure out which quads have ambiguous mappings
    //
    ////        for(Entry<Set<Set<Expr>>, QuadGroup>entry : cnfToQuadGroup.entrySet()) {
    ////            System.out.println(entry.getKey() + ": " + entry.getValue());
    ////        }
    //
    //        // Order the quad groups by number of candidates - least number of candidates first
    ////        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>(cnfToQuadGroup.values());
    //        Collections.sort(quadGroups, new Comparator<QuadGroup>() {
    //            @Override
    //            public int compare(QuadGroup a, QuadGroup b) {
    //                int i = getNumMatches(a);
    //                int j = getNumMatches(b);
    //                int r = j - i;
    //                return r;
    //            }
    //        });
    //
    //
    //        List<Iterable<Map<Var, Var>>> cartesian = new ArrayList<Iterable<Map<Var, Var>>>(quadGroups.size());
    //
    //        // TODO Somehow obtain a base mapping
    //        Map<Var, Var> baseMapping = Collections.<Var, Var>emptyMap();
    //
    //        for(QuadGroup quadGroup : quadGroups) {
    //            Iterable<Map<Var, Var>> it = IterableVarMapQuadGroup.create(quadGroup, baseMapping);
    //            cartesian.add(it);
    //        }
    //
    //        CartesianProduct<Map<Var, Var>> cart = new CartesianProduct<Map<Var,Var>>(cartesian);
    //
    //        Iterator<Map<Var, Var>> result = new IteratorVarMapQuadGroups(cart.iterator());
    //
    //        return result;
    //    }

        public static <K, V, R extends Map<K, V>> R mergeCompatible(Iterable<? extends Map<? extends K, ? extends V>> maps, Supplier<R> resultSupplier) {
            R result = resultSupplier.get();

            for(Map<? extends K, ? extends V> map : maps) {
                if(isCompatible(map, result)) {
                    result.putAll(map);
                } else {
                    result = null;
                    break;
                }
            }

            return result;
        }

//    public static <K, V, R extends Map<K, V>> R mergeCompatible(Map<K, V> a, Map<K, V> b, Supplier<R> resultSupplier) {
//        R result = mergeCompatible(Arrays.asList(a, b), resultSupplier);
//        return result;
//    }

    public static <K, V, R extends Map<K, V>> R mergeCompatible(Map<? extends K, ? extends V> a, Map<? extends K, ? extends V> b, Supplier<R> resultSupplier) {
        R result = mergeCompatible(Arrays.asList(a, b), resultSupplier);
        return result;
    }



    /*
     * Lazy map views based on a comment at https://github.com/google/guava/issues/912
     */

    /**
     * Returns a view of the union of two maps.
     *
     * For all keys k, if either map contains a value for k, the returned map contains that value. If both maps
     * contain a value for the same key, this map contains the value in the second of the two provided maps.
     */
    public static <K, V> Map<K, V> union(Map<K, ? extends V> a, Map<K, ? extends V> b) {
        return union(a, b, (v1, v2) -> v2);
    }

    /**
     * Returns a view of the union of two maps.
     *
     * For all keys k, if either map contains a value for k, the returned map contains that value. If both maps
     * contain a value for the same key, the conflict is resolved with the provided function.
     */
    public static <K, V> Map<K, V> union(
            Map<K, ? extends V> a,
            Map<K, ? extends V> b,
            BinaryOperator<V> conflictHandler) {
        return Maps.asMap(Sets.union(a.keySet(), b.keySet()),
                (K k) -> {
                    V r;
                    if (!a.containsKey(k)) {
                        r = b.get(k);
                    } else if (!b.containsKey(k)) {
                        r = a.get(k);
                    } else {
                        V v1 = a.get(k);
                        V v2 = b.get(k);
                        r = conflictHandler.apply(v1, v2);
                    }
                    return r;
                });
    }

    /**
     * Returns a view of the map where all keys present in 'deletions' are hidden
     */
    public static <K, V> Map<K, V> difference(
            Map<K, ? extends V> map,
            Set<? super K> deletions) {
        return Maps.asMap(Sets.difference(map.keySet(), deletions), map::get);
    }

}
