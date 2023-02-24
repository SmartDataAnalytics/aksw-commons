package org.aksw.commons.util.algebra;

import java.util.List;

public interface ExprOps<E, V> {
    List<E> getSubExprs(E expr);
    E copy(E proto, List<E> subExprs);

    boolean isFunction(E expr);
    V asVar(E expr); // Return null if not a var
    E varToExpr(V var);

    default boolean isVar(E expr) {
        return asVar(expr) != null;
    }
//
//    default boolean isConstant(E expr) {
//        boolean result = !isFunction(expr) && !isVar(expr);
//        return result;
//    }
}
