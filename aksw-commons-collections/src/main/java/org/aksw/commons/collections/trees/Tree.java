package org.aksw.commons.collections.trees;

import java.util.List;

public interface Tree<T> {
    T getRoot();
    List<T> getChildren(T node);
    T getParent(T node);

    T copy(T node, List<T> children);
    
    //boolean contains(Object node);
}
