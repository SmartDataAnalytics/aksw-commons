package org.aksw.commons.path.trav.api;

import org.aksw.commons.path.core.Path;

public interface TravProvider<S, V> {
    Trav<S, V> root();

    Trav<S, V> traverse(Trav<S, V> from, S segment);
    Trav<S, V> traverse(Trav<S, V> from, Path<S> path);
}
