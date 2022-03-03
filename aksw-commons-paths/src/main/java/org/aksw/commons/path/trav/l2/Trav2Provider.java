package org.aksw.commons.path.trav.l2;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.trav.l2.Travs2.Trav2A;
import org.aksw.commons.path.trav.l2.Travs2.Trav2B;

public interface Trav2Provider<T, S, A extends S, B extends S>
{

    default Trav2A<T, S, A, B> newRoot(Path<T> rootPath) {
        A a = mkRoot();
        return new Trav2A<T, S, A, B>(this, rootPath, null, a);
    }

    A mkRoot();

    B toB(Trav2A<T, S, A, B> a, T segment);
    A toA(Trav2B<T, S, A, B> b, T segment);
}
