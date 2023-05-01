package org.aksw.commons.util.algebra;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;

/**
 * Generic class for factorization of common sub expressions.
 *
 * Main application is to abstract factorization of Jena's Op and Expr hierarchy.
 *
 * @param <V> The type of variables.
 * @param <E> The type of expressions.
 */
public class GenericFactorizer<E, V> {
    protected ExprOps<E, V> exprOps;
    /** Predicate which can be used to stop descend down the expression tree */
    protected ExprFilter<E> isBlocker;

    public GenericFactorizer(ExprOps<E, V> exprOps, ExprFilter<E> isBlocker) {
        super();
        this.exprOps = exprOps;
        this.isBlocker = isBlocker;
    }

    public ExprOps<E, V> getExprOps() {
        return exprOps;
    }

    public ExprFilter<E> getIsBlocker() {
        return isBlocker;
    }


    public E factorize(E expr, BiMap<V, E> varCxt, Supplier<V> nextVar) {
        E result = factorize(null, 0, expr, varCxt, nextVar);
        return result;
    }

    public E factorize(E parent, int childIdx, E child, BiMap<V, E> cxt, Supplier<V> nextVar) {
        E result;
        if (isBlocker != null && isBlocker.test(parent, childIdx, child)) {
            result = child;
        } else {
            List<E> before = exprOps.getSubExprs(child);
            List<E> after = new ArrayList<>(before.size());
            for (int i = 0; i < before.size(); ++i) {
                E subExpr = before.get(i);
                E tmp = factorize(child, i, subExpr, cxt, nextVar);
                after.add(tmp);
            }
            E e = exprOps.copy(child, after);
            // Note: It shouldn't be necessary to allocate var names for other vars or constants            
            if (exprOps.isFunction(e)) {
                V var = cxt.inverse().get(e);
                if (var == null) {
                    var = nextVar.get();
                    cxt.put(var, e);
                }
                result = exprOps.varToExpr(var);
            } else {
                result = e;
            }
        }
        return result;
    }
}
