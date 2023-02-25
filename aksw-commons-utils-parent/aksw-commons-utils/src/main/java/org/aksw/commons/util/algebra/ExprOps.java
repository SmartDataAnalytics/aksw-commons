package org.aksw.commons.util.algebra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public interface ExprOps<E, V> {
    List<E> getSubExprs(E expr);
    E copy(E proto, List<E> subEs);

    boolean isFunction(E expr);
    V asVar(E expr); // Return null if not a var
    E varToExpr(V var);

    default boolean isVar(E expr) {
        return asVar(expr) != null;
    }

    /**
     * Returns all variables mentioned in an expression.
     * This method is provided for convenience.
     *
     * @implNote Relies on {@link ExprOps#varsMentioned(ExprOps, Object)}}
     */
    default Set<V> varsMentioned(E expr) {
        return ExprOps.varsMentioned(this, expr);
    }

    /** Computes the used variables. Prefer {@link ExprOps#varsMentioned(Object)} for applications. */
    public static <E, V> Set<V> varsMentioned(ExprOps<E, V> exprOps, E expr) {
        Set<V> result = Streams.stream(Traverser.forTree(exprOps::getSubExprs).depthFirstPostOrder(expr))
            .map(e -> exprOps.isVar(e) ? exprOps.asVar(e) : null)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    public static <E, V> Set<E> getLeafs(ExprOps<E, V> exprOps, E expr) {
        Set<E> result = Streams.stream(Traverser.forTree(exprOps::getSubExprs).depthFirstPostOrder(expr))
            .filter(e -> exprOps.getSubExprs(e).isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    public static <E, V> E replace(ExprOps<E, V> exprOps, E expr, Function<? super E, ? extends E> fn) {
        E result = fn.apply(expr);
        if (result == expr) { // No change yet
            List<E> subExprsBefore = exprOps.getSubExprs(expr);
            List<E> subExprsAfter = new ArrayList<>();
            boolean change = false;
            for (E subExprBefore : subExprsBefore) {
                E subExprAfter = replace(exprOps, subExprBefore, fn);
                if (subExprAfter != subExprBefore) {
                    change = true;
                }
                subExprsAfter.add(subExprAfter);
            }

            if (change) {
                result = exprOps.copy(expr, subExprsAfter);
            }
        }
        return result;
    }

//
//    default boolean isConstant(E expr) {
//        boolean result = !isFunction(expr) && !isVar(expr);
//        return result;
//    }
}
