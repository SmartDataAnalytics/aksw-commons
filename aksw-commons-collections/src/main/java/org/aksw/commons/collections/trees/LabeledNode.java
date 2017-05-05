package org.aksw.commons.collections.trees;

import java.util.Collection;

public interface LabeledNode<K>
    //extends Entry<K, T>
{
    K getKey();

    LabeledTree<K, ?> getTree();

    LabeledNode<K> getParent();
    Collection<? extends LabeledNode<K>> getChildren();
    //T getLabel();
}
