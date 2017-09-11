package org.aksw.commons.collections.trees;

import java.util.Collection;

public interface TreeNode<T> {
    Tree<T> getTree();
    T getNode();

    TreeNode<T> getParent();
    Collection<TreeNode<T>> getChildren();
}
