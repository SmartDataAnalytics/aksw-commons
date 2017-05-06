package org.aksw.commons.collections.trees;

import java.util.Collection;

public interface LabeledNode<K>
//    extends AutoCloseable
    //extends Entry<K, T>
{
    K getKey();

    LabeledTree<K, ?> getTree();

    LabeledNode<K> getParent();
    Collection<? extends LabeledNode<K>> getChildren();

    // Method that gets called when a node is deleted from the tree
    void destroy();
    //T getLabel();
}
