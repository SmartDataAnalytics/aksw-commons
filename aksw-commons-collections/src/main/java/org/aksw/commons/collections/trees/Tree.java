package org.aksw.commons.collections.trees;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Equivalence;

/**
 * Maybe a tree is an IndirectTree<K, K>, so that each node represents itself?
 * @author raven
 *
 * @param <T>
 */
public interface Tree<T> {
    T getRoot();
    Collection<T> getChildren(T node);
    T getParent(T node);

    //Equivalence<T> getEquivalence();

    /**
     * Copies a given node thereby setting the provided children as its children.
     *
     * @param node
     * @param children
     * @return A copy of the given node with the children set appropriately
     */
    T copy(T node, List<T> children);

    //TreeOps<T> getOps();

    //boolean contains(Object node);
    Tree<T> createNew(T root);

    long nodeCount();
}
