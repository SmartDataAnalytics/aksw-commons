package org.aksw.commons.path.stack;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.stack.Trav1X.Trav1;

public interface Trav1Provider<T, S>
{

    default Trav1<T, S> newRoot(Path<T> rootPath) {
        S a = mkRoot();
        return new Trav1<T, S>(this, rootPath, null, a);
    }

    S mkRoot();

    S next(Trav1<T, S> a, T segment);
}
