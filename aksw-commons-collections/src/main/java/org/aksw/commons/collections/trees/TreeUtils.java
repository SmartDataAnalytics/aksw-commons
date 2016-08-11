package org.aksw.commons.collections.trees;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TreeUtils {
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
     * Returns the set of nodes in each level of the tree
     * 
     * @param tree
     * @return
     */
    public static <T> List<Set<T>> nodesPerLevel(Tree<T> tree) {
        List<Set<T>> result = new ArrayList<>();
        
        Set<T> current = Collections.singleton(tree.getRoot());
        while(!current.isEmpty()) {
            result.add(current);
            Set<T> next = new LinkedHashSet<>();
            for(T node : current) {            
                List<T>children = tree.getChildren(node);
                next.addAll(children);
            }

            current = next;
        }
        
        return result;
    }

    public static <T> List<T> getLeafs(Tree<T> tree) {
        List<T> result = new ArrayList<T>();
        T root = tree.getRoot();
        getLeafs(result, tree, root);
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

}
