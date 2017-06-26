package org.aksw.commons.collections.trees;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.aksw.commons.collections.reversible.ReversibleMap;


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
        ReversibleMap<K, K> hierarchy = tree.childToParent;

        Collection<K> childKeys = hierarchy.reverse().get(id);

        return new AbstractCollection<X>() {
            @Override
            public Iterator<X> iterator() {
                Iterator<K> base = new ArrayList<>(childKeys).iterator();
                return new Iterator<X>() {
                    @Override
                    public boolean hasNext() { return base.hasNext(); }
                    @Override
                    public X next() { return tree.getNode(base.next()); }
                };
            }
            @Override
            public int size() {
                return childKeys.size();
            }
        };

        // Return a view which supports removals
        // Some hack to try removing both parent-child and child-parent relations
//        Collection<X> result = new AbstractCollection<X>() {
//
//            @Override
//            public Iterator<X> iterator() {
//                Iterator<K> base = childKeys.iterator();
//                //return childKeys.stream().map(tree::getNode).iterator();
//                return new SinglePrefetchIterator<X>() {
//                    K lastSeen = null;
//                    @Override
//                    public void remove() {
//                        base.remove();
//                        hierarchy.remove(lastSeen, id);
//                    }
//                    @Override
//                    protected X prefetch() throws Exception {
//                        if(!base.hasNext()) return this.finish();
//                        lastSeen = base.next();
//                        X r = tree.getNode(lastSeen);
//                        return r;
//                    }
//                };
//            }
//
//            @Override
//            public int size() {
//                return childKeys.size();
//            }
//        };
//
//        List<LabeledNode<K, T>> result = new ArrayList<>(childKeys.size());
//        for(K childKey : childKeys) {
//            LabeledNode<K, T> childNode = tree.getNode(childKey);
//            result.add(childNode);
//        }
//
//        return result;
    }

    @Override
    public LabeledTree<K, X> getTree() {
        return tree;
    }

//    @Override
    public K getKey() {
        return id;
    }

    @Override
    public String toString() {
        return "Node[" + id + (!tree.keyToNode.containsKey(id) ? " detached": "") + "], parent: " + tree.childToParent.get(id) + " children: " + tree.childToParent.reverse().get(id);
    }

    @Override
    public void destroy() {
        // Nothing to do by default
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
