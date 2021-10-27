package org.aksw.commons.util.triplet;

/** A generalization of a triple; an edge in a graph with node type V and edge attribute type E */
public interface Triplet<V, E> {
    V getSubject();
    E getPredicate();
    V getObject();

    public static <V, E> Triplet<V, E> swap(Triplet<V, E> t) {
        Triplet<V, E> result = new TripletImpl<>(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
    }

}
