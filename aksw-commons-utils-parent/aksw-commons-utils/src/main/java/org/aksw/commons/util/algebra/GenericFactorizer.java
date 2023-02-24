package org.aksw.commons.util.algebra;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public GenericFactorizer(ExprOps<E, V> exprOps) {
        super();
        this.exprOps = exprOps;
    }

    public ExprOps<E, V> getExprOps() {
        return exprOps;
    }

    public E factorize(E expr, BiMap<V, E> cxt, Supplier<V> nextVar) {
        List<E> before = exprOps.getSubExprs(expr);
        List<E> after = new ArrayList<>(before.size());
        for (E subExpr : before) {
            E tmp = factorize(subExpr, cxt, nextVar);
            after.add(tmp);
        }
        E e = exprOps.copy(expr, after);
        E result;
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
        return result;
    }
}
