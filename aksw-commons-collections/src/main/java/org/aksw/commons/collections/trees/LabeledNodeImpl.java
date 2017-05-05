package org.aksw.commons.collections.trees;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * A node view on a tree - all functions delegate to the underlying tree object
 *
 * Not sure if a node should inherit from Entry or if there should be a 'asEntry' method.
 * It seems useful for a node being able to act as an association between its key and its label
 *
 * @author raven
 *
 * @param <K>
 * @param <T>
 */
public class LabeledNodeImpl<K, X extends LabeledNode<K>, Y extends LabeledTree<K, X>>
    implements LabeledNode<K>//, Entry<K, T>
{
    protected Y tree;
    protected K id;

    public LabeledNodeImpl(Y tree, K id) {
        super();
        this.tree = tree;
        this.id = id;
    }

    @Override
    public LabeledNode<K> getParent() {
        K parentKey = tree.getParent(id);
        LabeledNode<K> result = tree.getNode(parentKey);
        return result;
    }


    public void appendChild(LabeledNode<K> child) {
        // TODO Validation
        tree.childToParent.put(child.getKey(), id);
    }

    @Override
    public Collection<X> getChildren() {
        Collection<K> childKeys = tree.getChildren(id);

        // Return a view
        Collection<X> result = new AbstractCollection<X>() {

            @Override
            public Iterator<X> iterator() {
                Iterator<K> base = childKeys.iterator();
                //return childKeys.stream().map(tree::getNode).iterator();
                return new Iterator<X>() {
                    @Override
                    public boolean hasNext() { return base.hasNext(); }
                    @Override
                    public X next() { return tree.getNode(base.next()); }
                    @Override
                    public void remove() { base.remove(); }
                };
            }

            @Override
            public int size() {
                return childKeys.size();
            }
        };
//
//        List<LabeledNode<K, T>> result = new ArrayList<>(childKeys.size());
//        for(K childKey : childKeys) {
//            LabeledNode<K, T> childNode = tree.getNode(childKey);
//            result.add(childNode);
//        }

        return result;
    }

    @Override
    public LabeledTree<K, X> getTree() {
        return tree;
    }

//    @Override
    public K getKey() {
        return id;
    }

//    @Override
//    public T getValue() {
//        T result = tree.getLabel(id);
//        return result;
//    }

//    @Override
//    public T setValue(T value) {
//        T result = tree.setLabel(id, value);
//        return result;
//    }
}
