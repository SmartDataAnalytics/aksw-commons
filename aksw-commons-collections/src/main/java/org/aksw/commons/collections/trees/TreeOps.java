package org.aksw.commons.collections.trees;

import java.util.List;
import java.util.function.Function;

public interface TreeOps<T> {
    /**
     * Create a new tree from the given root node
     * 
     * @param root
     */    
    Tree<T> createNew(T root);
    boolean isEquivalent(T a, T b);

    //Function<T, List<T>> parentToChild
    boolean canChangeChildrenInPlace();
    
    
    
}
