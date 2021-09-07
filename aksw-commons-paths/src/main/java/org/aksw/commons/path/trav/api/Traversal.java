package org.aksw.commons.path.trav.api;

import org.aksw.commons.path.core.Path;

public interface Traversal<
    V,
    S,
    P extends Path<S>,
    T extends Traversal<V, S, P, T>> {

    P getPath();
    T back();
    V getValue();

    T traverse(Path<? extends S> path);
}
