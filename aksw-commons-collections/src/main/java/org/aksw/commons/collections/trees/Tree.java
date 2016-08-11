package org.aksw.commons.collections.trees;

import java.util.List;

public interface Tree<T> {
    T getRoot();
    List<T> getChildren(T node);
    T getParent(T node);
    
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
}
