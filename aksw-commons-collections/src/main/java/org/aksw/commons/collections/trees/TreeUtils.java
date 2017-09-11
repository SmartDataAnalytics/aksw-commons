package org.aksw.commons.collections.trees;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.multimaps.MultimapUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class TreeUtils {


    public static <T> Multimap<T, T> groupByParent(Tree<T> tree, Collection<T> nodes, Multimap<T, T> result) {
        MultimapUtils.groupBy(nodes, tree::getParent, result);
        return result;
    }

//	Predicate<T> p = x -> !(x instanceof OpService);
    /**
     * Given a predicate, return the minimum set of nodes, for which all nodes in their subtree satisfy the predicate.
     * The algo starts with the set X of leaf nodes satisfying the predicate, and moves upwards.
     * If all children of a parent satisfy the predicate, the children are removed from X and the parent is added instead.
     *
     * Note: Uses IdentityHashSet
     *
     * @param tree
     * @param predicate
     * @return
     */
    public static <T> Set<T> propagateBottomUpLabel(Tree<T> tree, Predicate<T> predicate) {
        Collection<T> leafs = TreeUtils.getLeafs(tree);

        Set<T> result = leafs.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(Sets::newIdentityHashSet));

        for(;;) {
            //List<Op> parents = levels.get(i);
            Set<T> parents = result.stream()
                    .map(tree::getParent)
                    .filter(x -> x != null)
                    .collect(Collectors.toCollection(Sets::newIdentityHashSet));

            boolean anyMatch = false;
            for(T parent : parents) {
                Collection<T> children = tree.getChildren(parent);
                boolean allChildrenTagged = children.stream().allMatch(result::contains);

                if(allChildrenTagged) {
                    anyMatch = true;
                    result.removeAll(children);
                    result.add(parent);
                }
            }

            if(!anyMatch) {
                break;
            }
        }

        return result;
    }


    // TODO We may want to add a sub-tree function, which allows using any of a base-tree's node as the root
    public static <T> Tree<T> subTree(Tree<T> tree, T newRoot) {
        Tree<T> result = new SubTree<>(tree, newRoot);
        return result;
    }

    public static <T> long nodeCount(Tree<T> tree) {
        long result = Collections.singleton(tree.getRoot()).stream()
                .flatMap(x -> tree.getChildren(x).stream())
                .count();

        return result;

    }

    public static <T> long depth(Tree<T> tree) {
        T root = tree.getRoot();
        long result = depth(tree, root);
        return result;

    }

    public static <T> long depth(Tree<T> tree, T node) {
        long result;
        if(node == null) {
            result = 0;
        } else {
            Collection<T> children = tree.getChildren(node);
            result = 1l + children.stream()
                    .mapToLong(child -> depth(tree, child))
                    .max()
                    .orElse(0l);
        }

        return result;
    }


    public static <T> int childIndexOf(TreeOps2<T> ops, T node) {
        List<T> children = ops.getParentToChildren().apply(node);
        int result = children.indexOf(node);
        return result;
    }

    //  Replaces a node in the tree - returns a new tree object.
    public static <T> Tree<T> replace(Tree<T> tree, T node, T replacement) {
        // TODO Make the equivalence test configurable
        T newRoot = replaceNode(tree, node, replacement, (a, b) -> a == b);

        Tree<T> result = tree.createNew(newRoot); //TreeImpl.create(newRoot, tree.getOps());
        return result;
    }

    public static <T> int indexOf(List<T> list, T find, BiPredicate<? super T, ? super T> isEquiv) {
        Iterator<T> it = list.iterator();
        int i = 0;
        boolean found = false;
        while(it.hasNext()) {
            T item = it.next();
            found = isEquiv.test(item, find);
            if(found) {
                break;
            }
        }

        int result = found ? i : -1;
        return result;
    }

    public static <T> T replaceNode(Tree<T> tree, T node, T replacement, BiPredicate<? super T, ? super T> isEquiv) {
        T result;

        if(node == null) {
             result = replacement;
        } else {
            T parent = tree.getParent(node);
            List<T> children = new ArrayList<T>(tree.getChildren(parent));
            int i = indexOf(children, node, isEquiv);
            //int i = children.indexOf(node);


            children.set(i, replacement);
            T parentReplacement = tree.copy(parent, children);

            result = replaceNode(tree, parent, parentReplacement, isEquiv);
        }

        return result;
    }



    public static <T> T substitute(
            T node,
            boolean descendIntoSubst,
            TreeOps2<T> ops,
            Function<? super T, ? extends T> transformFn
            )
    {
        T result = substitute(node, descendIntoSubst, ops.getParentToChildren(), transformFn, ops.getReplaceChildren());
        return result;
    }

    public static <T> T substitute(
            T node,
            boolean descendIntoSubst,
            Function<? super T, ? extends Collection<? extends T>> getChildrenFn,
            Function<? super T, ? extends T> transformFn,
            BiFunction<T, List<T>, T> copyFn)
    {
        T tmp = transformFn.apply(node);

        // Descend into op if tmp was null (assigned in statement after this)
        // or descend into the replacement op
        boolean descend = tmp == null || descendIntoSubst;

        // Use op if tmp is null
        tmp = tmp == null ? node : tmp;

        T result;
        if(descend) {
            List<T> newSubOps = getChildrenFn.apply(tmp).stream()
                .map(subOp -> substitute(subOp, descendIntoSubst, getChildrenFn, transformFn, copyFn))
                .collect(Collectors.toList());

            result = copyFn.apply(node, newSubOps); //OpUtils.copy(op, newSubOps);
        } else {
            result = tmp;
        }

        return result;
    }


    /**
     * Find the first ancestor for which the predicate evaluates to true
     * @param tree
     * @param node
     * @param predicate
     *
     * @return
     */
    public static <T> T findAncestor(Tree<T> tree, T node, java.util.function.Predicate<T> predicate) {
        T current = node;
        do {
            current = tree.getParent(current);
        } while(current != null && !predicate.test(current));

        return current;
    }

    public static <T> Stream<T> inOrderSearch(T node, Function<T, ? extends Iterable<T>> parentToChildren) {
        Stream<T> result = Stream.of(node);

        Iterable<T> children = parentToChildren.apply(node);
        Stream<T> childStream = StreamSupport.stream(children.spliterator(), false);

        result = Stream.concat(
                result,
                childStream.flatMap(c -> inOrderSearch(
                        c,
                        parentToChildren
                )));

        return result;

    }

    /**
     * In-order-search starting from the given node and descending into the tree.
     * Each node may be mapped to a value.
     * A predicate determines whether to stop descending further into a sub-tree.
     * Useful for extracting patterns from a tree.
     *
     * @param node
     * @param parentToChildren
     * @param nodeToValue
     * @param doDescend
     * @return
     */
    public static <T, V> Stream<Entry<T, V>> inOrderSearch(
            T node,
            Function<T, ? extends Iterable<T>> parentToChildren,
            Function<? super T, V> nodeToValue,
            BiPredicate<T, V> doDescend
            ) {

        V value = nodeToValue.apply(node);
        Entry<T, V> e = new SimpleEntry<>(node, value);
        boolean descend = doDescend.test(node, value);

        Stream<Entry<T, V>> result = Stream.of(e);
        if(descend) {
            Iterable<T> children = parentToChildren.apply(node);
            Stream<T> childStream = StreamSupport.stream(children.spliterator(), false);

            result = Stream.concat(
                    result,
                    childStream.flatMap(c -> inOrderSearch(
                            c,
                            parentToChildren,
                            nodeToValue,
                            doDescend)));
        }

        return result;
    }



    /**
     * For each level, yield the inner nodes
     *
     * The root node will always be part of the list, even if it does not have children
     */
    public static <T> List<List<T>> innerNodesPerLevel(Tree<T> tree) {
        List<List<T>> result = new ArrayList<>();

        //Set<T> current = Collections.singleton(tree.getRoot());
        T root = tree.getRoot();
        List<T> current = Collections.singletonList(root);
        while(!current.isEmpty()) {
            List<T> next = new ArrayList<>();//current.size());

            //            result.add(current);
            //Set<T> next = new LinkedHashSet<>();
            //List<T> next = new ArrayList<>(current.size());
            for(T node : current) {
                Collection<T> children = tree.getChildren(node);
                if(!children.isEmpty() || node == root) {
                    next.add(node);
                }

                //next.addAll(children);
            }

            if(!next.isEmpty()) {
                result.add(next);
            }

            current = next;
        }

        return result;
    }

    /**
     * Returns the set of nodes in each level of the tree
     * The set containing the root will be the first item in the list
     *
     *
     * @param tree
     * @return
     */
    public static <T> List<List<T>> nodesPerLevel(Tree<T> tree) {
        List<List<T>> result = new ArrayList<>();

        //Set<T> current = Collections.singleton(tree.getRoot());
        List<T> current = Collections.singletonList(tree.getRoot());
        while(!current.isEmpty()) {
            result.add(current);
            //Set<T> next = new LinkedHashSet<>();
            List<T> next = new ArrayList<>();
            for(T node : current) {
                Collection<T> children = tree.getChildren(node);
                next.addAll(children);
            }

            current = next;
        }

        return result;
    }

    public static <T> List<T> getLeafs(Tree<T> tree) {
        T root = tree.getRoot();
        List<T> result = inOrderSearch(root, tree::getChildren)
            .filter(node -> node == null ? true : tree.getChildren(node).isEmpty())
            .collect(Collectors.toList());

//        List<T> result = new ArrayList<T>();
//        T root = tree.getRoot();
//        getLeafs(result, tree, root);
        return result;
    }

    public static <T> void getLeafs(Collection<T> result, Tree<T> tree, T node) {
        Collection<T> children = tree.getChildren(node);
        if(children.isEmpty()) {
            result.add(node);
        } else {
            for(T child : children) {
                getLeafs(result, tree, child);
            }
        }
    }

    /**
     * Get the set of immediate parents for a given set of children
     *
     * @param tree
     * @param children
     * @return
     */
    public static <T> Set<T> getParentsOf(Tree<T> tree, Iterable<T> children) {
        Set<T> result = new HashSet<T>();
        for(T child: children) {
            T parent = tree.getParent(child);
            result.add(parent);
        }

        return result;
    }

    /**
     * Traverse an op structure and create a map from each subOp to its immediate parent
     *
     * NOTE It must be ensured that common sub expressions are different objects,
     * since we are using an identity hash map for mapping children to parents
     *
     *
     * @param op
     * @return
     */
    public static <T> Map<T, T> parentMap(T root, Function<T, List<T>> parentToChildren) {
        Map<T, T> result = new IdentityHashMap<T, T>();

        result.put(root, null);

        parentMap(result, root, parentToChildren);
        return result;
    }


    public static <T> void parentMap(Map<T, T> result, T parent, Function<T, List<T>> parentToChildren) {
        List<T> children = parentToChildren.apply(parent);

        for(T child : children) {
            result.put(child, parent);

            parentMap(result, child, parentToChildren);
        }
    }


    /**
     * Create a new tree object which has certain nodes remapped with *leaf* nodes
     *
     * @param tree
     * @param remapFn
     * @return
     */
    public static <T> Tree<T> remapSubTreesToLeafs(Tree<T> tree, Function<? super T, ? extends T> remapFn) {
        Map<T, T> fwd = TreeUtils
                .inOrderSearch(
                        tree.getRoot(),
                        tree::getChildren,
                        remapFn,
                        (opNode, value) -> value == null) // descend while the value is null
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, IdentityHashMap::new));

        Map<T, T> bwd = new IdentityHashMap<>();
        for(Entry<T, T> e : fwd.entrySet()) {
            bwd.put(e.getValue(), e.getKey());
        }

        Tree<T> result = new TreeReplace<>(tree, fwd, bwd);
        return result;
    }

    public static <T> Tree<T> removeUnaryNodes(Tree<T> tree) {
        ListMultimap<T, T> parentToChildren = MultimapUtils.newIdentityListMultimap(); //ArrayListMultimap.create();
        T newRoot = removeUnaryNodes(tree, tree.getRoot(), parentToChildren);

        Tree<T> result = newRoot == null
                ? null
                : TreeImpl.create(newRoot, (node) -> parentToChildren.get(node));

        return result;
    }

    public static <T> T removeUnaryNodes(Tree<T> tree, T node, ListMultimap<T, T> parentToChildren) {
        Collection<T> children = tree.getChildren(node);
        int childCount = children.size();

        T result;
        switch(childCount) {
        case 0:
            result = node;
            break;
        case 1:
            T child = children.iterator().next();
            result = removeUnaryNodes(tree, child, parentToChildren);
            break;
        default:
            result = node;
            for(T c : children) {
                T newChild = removeUnaryNodes(tree, c, parentToChildren);
                parentToChildren.put(node,  newChild);
            }
            break;
        }

        return result;
    }

    // TODO: Another output format: Map<Entry<T, T>, Multimap<T, T>>
    /**
     * Input: A mapping from cache nodes to candidate query nodes represented as a Multimap<T, T>.
     * Output: The mapping partitioned by each node's first multiary ancestor.
     *
     * Output could also be: Multimap<Op, Op> - fmaToNodesCache
     *
     *
     * For every cacheFma, map to the corresponding queryFmas - and for
     * each of these mappings yield the candidate node mappings of the children
     * Multimap<OpCacheFma, Map<OpQueryFma, Multimap<OpCache, OpQuery>>>
     *
     * Q: What if cache nodes do not have a fma?
     * A: In this case the fma would be null, which means that there can only be a single cache node
     * which would be grouped with a null fma.
     * In the query, we can then check whether we are pairing a union with another union or null.
     *
     * We always map from cache to query.
     *
     * Map<CacheFma, QueryFma>
     *
     * So the challenge is now again how to represent all the facts and how to perform
     * the permutations / combinations...
     *
     *
     *
     *
     *
     * @param cacheTree
     * @param tree
     * @param cacheToQueryCands
     * @return
     */
    public static <T> Map<T, Multimap<T, T>> clusterNodesByFirstMultiaryAncestor(Tree<T> tree, Multimap<T, T> mapping) { //Collection<T> nodes) {
        Map<T, Multimap<T, T>> result = new HashMap<>();

        Set<Entry<T, Collection<T>>> entries = mapping.asMap().entrySet();
        for(Entry<T, Collection<T>> entry : entries) {
            T node = entry.getKey();
            //T multiaryAncestor = firstMultiaryAncestor(tree, cacheNode);
            T multiaryAncestor = tree.getParent(node);
            Collection<T> queryNodes = entry.getValue();

            for(T targetNode : queryNodes) {
                Multimap<T, T> mm = result.computeIfAbsent(multiaryAncestor, (k) -> HashMultimap.<T, T>create());
                mm.put(node, targetNode);
            }
        }

        return result;
    }

    /**
     * Return a node's first ancestor having an arity > 1
     * null if there is none.
     *
     * @param tree
     * @param node
     * @return
     */
    public static <T> T firstMultiaryAncestor(Tree<T> tree, T node) {
        T result = null;
        T current = node;
        while(current != null) {
            T parent = tree.getParent(result);
            Collection<T> children = tree.getChildren(parent);
            int arity = children.size();
            if(arity > 1) {
                result = parent;
                break;
            }
            current = parent;
        }
        return result;
    }

    public static <X> List<X> getUnaryAncestors(X x, Tree<X> tree, Tree<X> multiaryTree) {
        List<X> result = new ArrayList<>();

        X ancestor = multiaryTree.getParent(x);

        X currentNode = x;
        while((currentNode = tree.getParent(currentNode)) != null && !currentNode.equals(ancestor)) {
            result.add(currentNode);
        }


        return result;
    }

    // TODO How to handle root nodes / null values?
    /**
     * Given a mapping of child nodes, determine which parents may be mapped to each other.
     * For any nodes mapped in 'childMapping', their parents may be mapped as well.
     *
     *
     * @param aTree
     * @param bTree
     * @param childMapping
     * @return
     */
    public static <A, B> Multimap<A, B> deriveParentMapping(Tree<A> aTree, Tree<B> bTree, Multimap<A, B> childMapping) {
        Multimap<A, B> result = HashMultimap.create();
        Set<A> as = childMapping.keySet();
        for(A a : as) {
            A aParent = aTree.getParent(a);
            Collection<B> bs = childMapping.get(a);
            Set<B> bParents = getParentsOf(bTree, bs);

            result.putAll(aParent, bParents);
        }

        return result;
    }

}
