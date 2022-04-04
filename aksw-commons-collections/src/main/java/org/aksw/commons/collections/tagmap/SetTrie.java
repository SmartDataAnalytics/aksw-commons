package org.aksw.commons.collections.tagmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Set-based trie implementation for fast answering of super and subset queries.
 * Implementation is based on the publication
 * "Index Data Structure for Fast Subset and Superset Queries" by "Iztok Savnik"
 *
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class SetTrie<K, V> {

    protected class SetTrieNode
    {
        public SetTrieNode(long id, SetTrieNode parent, V value) {
            super();
            this.id = id;
            this.parent = parent;
            this.value = value;
            this.nextValueToChild = null; //new TreeMap<>();//HashMap<>();
        }

        protected long id;
        SetTrieNode parent;
        V value;
        NavigableMap<V, SetTrieNode> nextValueToChild;
        Map<K, NavigableSet<V>> keyToSet;


        @Override
        public String toString() {
            SetTrieNode c = this;
            List<V> vs = new ArrayList<>();
            while(c != superRootNode) {
                vs.add(c.value);
                c = c.parent;
            }
            Collections.reverse(vs);

            return "SetTrie node for " + vs + " with associated keys " + keyToSet.keySet();
        }
    }

    protected Comparator<? super V> comparator;

    // Mapping from key to node - used for fast removal
    protected Map<K, SetTrieNode> keyToNode = new HashMap<>();

    // An id counter for human readable node ids - may be useful for debugging / logging
    protected long nextId = 0;

    // Root node of the trie datastructure
    protected SetTrieNode superRootNode = new SetTrieNode(nextId++, null, null);

    public SetTrie(Comparator<? super V> comparator) {
        super();
        this.comparator = comparator;
    }

    public void clear() {
        keyToNode.clear();
        superRootNode.keyToSet = null; //.clear();
        superRootNode.nextValueToChild = null;
    }

    // TODO We may want to allow views on the Trie
    // HashSet (using a HashMap for self-containedness of the implementation) of current root nodes
    // protected Map<SetTrieNode, Void> rootNodes = new IdentityHashMap<>();

    public Set<V> put(K key, Collection<V> set) {
        // Remove any possibly existing prior association with the key
        Set<V> result = remove(key);

        // Note: The second argument is effectless, as we cannot encounter item type errors here
        NavigableSet<V> navSet = createNavigableSet(set, true);
        Iterator<V> it = navSet.iterator();

        SetTrieNode currentNode = superRootNode;
        while(it.hasNext()) {
            V v = it.next();

            SetTrieNode nextNode = currentNode.nextValueToChild == null ? null : currentNode.nextValueToChild.get(v);
            if(nextNode == null) {
                nextNode = new SetTrieNode(nextId++, currentNode, v);
                if(currentNode.nextValueToChild == null) {
                    currentNode.nextValueToChild = new TreeMap<>(comparator);
                }
                currentNode.nextValueToChild.put(v, nextNode);
            }
            currentNode = nextNode;
        }

        if(currentNode.keyToSet == null) {
            currentNode.keyToSet = new HashMap<>();
        }

        currentNode.keyToSet.put(key, navSet);

        keyToNode.put(key, currentNode);

        return result;
    }

    public Set<V> remove(Object key) {
        Set<V> result = null;

        SetTrieNode currentNode = keyToNode.get(key);

        if(currentNode != null && currentNode.keyToSet != null) {
            result = currentNode.keyToSet.remove(key);
            if(currentNode.keyToSet.isEmpty()) {
                currentNode.keyToSet = null;
            }
        }

        while(currentNode != null && currentNode.parent != null) {
            if(currentNode.nextValueToChild == null && currentNode.keyToSet == null) {
                currentNode.parent.nextValueToChild.remove(currentNode.value);
                if(currentNode.parent.nextValueToChild.isEmpty()) {
                    currentNode.parent.nextValueToChild = null;
                }

                currentNode = currentNode.parent;
            } else {
                currentNode = null;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public NavigableSet<V> createNavigableSet(Collection<?> set, boolean skipItemTypeErrors) {
        NavigableSet<V> result = new TreeSet<V>(comparator);

        for(Object o : set) {
            V v;
            try {
                v = (V)o;
            } catch(Exception e) {
                if(!skipItemTypeErrors) {
                    result = null;
                    break;
                } else {
                    continue;
                }
            }
            // Note: We can still encounter exceptions such as caused by the comparator
            result.add(v);
        }

        return result;
    }

    public Map<K, Set<V>> getAllSubsetsOf(Collection<?> set) {
        Set<V> navSet = createNavigableSet(set, false);

        Map<K, Set<V>> result = new HashMap<>();

        if(navSet == null) {
            // Set creation failed due to at least one item having an incorrect datatype.
            // Hence, there cannot be any superset
        } else {
            Iterator<V> it = navSet.iterator();

            List<SetTrieNode> frontier = new ArrayList<>();
            frontier.add(superRootNode);

            List<SetTrieNode> nextNodes = new ArrayList<>();

            // For every value, extend the frontier with the successor nodes for that value.
            while(it.hasNext()) {
                Object v = it.next();

                nextNodes.clear();
                for(SetTrieNode currentNode : frontier) {
                    SetTrieNode nextNode = currentNode.nextValueToChild == null ? null : currentNode.nextValueToChild.get(v);
                    if(nextNode != null) {
                        nextNodes.add(nextNode);
                    }
                }
                frontier.addAll(nextNodes);
            }


            // Copy all data entries associated with the frontier to the result
            for(SetTrieNode currentNode : frontier) {
                if(currentNode.keyToSet != null) {
                    for(Entry<K, NavigableSet<V>> e : currentNode.keyToSet.entrySet()) {
                        result.put(e.getKey(), e.getValue());
                    }
                }
            }
        }

        return result;
    }




    public Map<K, Set<V>> getAllSupersetsOf(Collection<?> set) {
        Map<K, Set<V>> result = coreGetAllSupersetsOf(set, 1);
        return result;
    }

    /**
     * mode:
     *  0: equivalence only
     *  1: supersets (equivalence + strict supersets)
     *  2: strict supersets
     *
     * @param set
     * @param mode
     * @return
     */
    public Map<K, Set<V>> coreGetAllSupersetsOf(Collection<?> set, int mode) {
        // Skip elements in the collection having an incorrect type, as we are looking for subsets which simply
        // cannot contain the conflicting items
        Set<V> navSet = createNavigableSet(set, true);
        Iterator<V> it = navSet.iterator();

        List<SetTrieNode> frontier = new ArrayList<>();
        frontier.add(superRootNode);

        // For every value, extend the frontier with the successor nodes for that value.
        V from = null;
        V upto = null;

        // Use a flag for null safety so we do not rely on the comparator to treat null as the least element
        boolean isLeastFrom = true;
        while(it.hasNext()) {
            from = upto;
            upto = it.next();

            List<SetTrieNode> nextNodes = new ArrayList<>();

            // Based on the frontier, we need to keep scanning nodes whose values is in the range [from, upto]
            // until we find the nodes whose values equals upto
            // Only these nodes then constitute the next frontier
            Collection<SetTrieNode> currentScanNodes = frontier;
            do {
                Collection<SetTrieNode> nextScanNodes = new ArrayList<>();
                for(SetTrieNode currentNode : currentScanNodes) {
                    if(currentNode.nextValueToChild != null) {
                        NavigableMap<V, SetTrieNode> candidateNodes = isLeastFrom
                                ? currentNode.nextValueToChild.headMap(upto, true)
                                : currentNode.nextValueToChild.subMap(from, true, upto, true);

                        for(SetTrieNode candidateNode : candidateNodes.values()) {
                            if(Objects.equals(candidateNode.value, upto)) {
                                nextNodes.add(candidateNode);
                            } else {
                                nextScanNodes.add(candidateNode);
                            }
                        }
                    }
                }
                currentScanNodes = nextScanNodes;
            } while(!currentScanNodes.isEmpty());

            frontier = nextNodes;

            isLeastFrom = false;
        }

        Map<K, Set<V>> result = new HashMap<>();

        // Copy all data entries associated with the frontier to the result
        Stream<SetTrie<K, V>.SetTrieNode> stream = frontier.stream();

        if(mode != 0) {
            stream = stream.flatMap(node -> reachableNodesAcyclic(
                        node,
                        x -> (x.nextValueToChild != null ? x.nextValueToChild.values() : Collections.<SetTrieNode>emptySet()).stream()));
        }

        stream.forEach(currentNode -> {
            if(currentNode.keyToSet != null) {
                for(Entry<K, NavigableSet<V>> e : currentNode.keyToSet.entrySet()) {
                    result.put(e.getKey(), e.getValue());
                }
            }
        });

        return result;
    }

    public Map<K, Set<V>> getAllEquisetsOf(Collection<?> prototype) {
        Map<K, Set<V>> result = coreGetAllSupersetsOf(prototype, 0);
        return result;
    }

    public static <T> Stream<T> reachableNodesAcyclic(T start, Function<T, Stream<T>> nav) {
        Stream<T> result = Stream.concat(
                Stream.of(start),
                nav.apply(start).flatMap(v -> reachableNodesAcyclic(v, nav)));
        return result;
    }

}

