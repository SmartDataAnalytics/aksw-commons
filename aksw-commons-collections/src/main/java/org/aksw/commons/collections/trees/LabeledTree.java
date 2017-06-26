package org.aksw.commons.collections.trees;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;


public class LabeledTree<K, X extends LabeledNode<K>>
    implements Tree<K>
{
    protected X rootNode;

    protected Map<K, X> keyToNode;

    protected K root;
    //protected ListMultimap<K, K> parentToChildren;
    //protected Map<K, K> childToParent;
    protected ReversibleMap<K, K> childToParent;
    //protected Map<K, T> nodeToData;

    protected ReclaimingSupplier<K> idSupplier;

    public LabeledTree() {
        this(null, new ReversibleMapImpl<>()); //, new HashMap<>());
    }
//
//    protected abstract LabeledNode<K, T> createNode();
//
//    protected removeNode(K id) {
//
//    }

    public LabeledTree(K root, ReversibleMap<K, K> childToParent) {// Map<K, T> nodeToData) {
        super();
        this.root = root;
        this.childToParent = childToParent;
        this.keyToNode = new HashMap<>();
        //this.nodeToData = nodeToData;
    }

    @Override
    public K getRoot() {
        return root;
    }
//
//    public T getLabel(K key) {
//        T result = nodeToData.get(key);
//        return result;
//    }
//
//    public T setLabel(K key, T value) {
//        // TODO Validate that the node exists
//        T result = nodeToData.put(key, value);
//        return result;
//    }


    public X getNode(K key) {
        X result = keyToNode.get(key);
        return result;
    }


    @Override
    public Collection<K> getChildren(K node) {
        return childToParent.reverse().get(node);
    }

    @Override
    public K getParent(K node) {
        return childToParent.get(node);
    }

    public X deleteNode(K node) {
        X result = keyToNode.get(node);

        // Call destroy before unlinking the node
        if(result != null) {
            result.destroy();
        }

        keyToNode.remove(node);
        childToParent.remove(node);

        return result;
    }
//
//    public static <K, T> LabeledTree<K, T> create(T root, Function<T, Collection<T>> getChildren, Supplier<K> keySupplier) {
//
//        K rootKey = null;
//        ReversibleMap<K, K> childToParent = new ReversibleMapImpl<>();
//        Map<K, T> nodeToData = new HashMap<>();
//
//
//        LabeledTree<K, T> result = new LabeledTree<K, T>(rootKey, childToParent, nodeToData);
//        return result;
//    }

    @Override
    public K copy(K node, List<K> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree<K> createNew(K root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long nodeCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return
            "LabeledTree [root=" + root + "]"
                + childToParent.reverse().toString();
    }


}
