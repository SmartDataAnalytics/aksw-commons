package org.aksw.commons.path.trav.l3;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.trav.l3.Trav3.Trav3A;
import org.aksw.commons.path.trav.l3.Trav3.Trav3B;
import org.aksw.commons.path.trav.l3.Trav3.Trav3C;

public interface Trav3Provider<T, S, A extends S, B extends S, C extends S>
{

    default Trav3A<T, S, A, B, C> newRoot(Path<T> rootPath) {
        A a = mkRoot();
        return new Trav3A<T, S, A, B, C>(this, rootPath, null, a);
    }

    A mkRoot();

    B toB(Trav3A<T, S, A, B, C> a, T segment);
    C toC(Trav3B<T, S, A, B, C> b, T segment);
    A toA(Trav3C<T, S, A, B, C> c, T segment);
}
