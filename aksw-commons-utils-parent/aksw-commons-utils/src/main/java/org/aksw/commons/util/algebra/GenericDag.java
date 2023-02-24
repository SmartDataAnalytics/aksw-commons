package org.aksw.commons.util.algebra;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class GenericDag<E, V> {
    protected GenericFactorizer<E, V> exprFactorizer;
    protected Supplier<V> nextVar;
    protected BiMap<V, E> varToExpr = HashBiMap.create();
    protected Set<E> roots = new LinkedHashSet<>();

    public GenericDag(GenericFactorizer<E, V> exprFactorizer, Supplier<V> nextVar) {
        super();
        this.exprFactorizer = exprFactorizer;
        this.nextVar = nextVar;
    }

    public ExprOps<E, V> getExprOps() {
        return exprFactorizer.getExprOps();
    }

    /** Factorize the argument expression against the state of this object */
    public E factorize(E expr) {
        E result = exprFactorizer.factorize(expr, varToExpr, nextVar);
        return result;
    }

    /** Call {@link #factorize(Object)} and add the result as a new root */
    public void addRoot(E expr) {
        E newRoot = factorize(expr);
        roots.add(newRoot);
    }

    public GenericFactorizer<E, V> getExprFactorizer() {
        return exprFactorizer;
    }

    public BiMap<V, E> getVarToExpr() {
        return varToExpr;
    }

    public Set<E> getRoots() {
        return roots;
    }

    public Multimap<V, V> getChildToParent() {
        Multimap<V, V> result = HashMultimap.create();
        for (Entry<V, E> e : varToExpr.entrySet()) {
            V parent = e.getKey();
            ExprOps<E, V> exprOps = getExprOps();
            List<E> exprs = exprOps.getSubExprs(e.getValue());
            for (E expr : exprs) {
                if (exprOps.isVar(expr)) {
                    V child = exprOps.asVar(expr);
                    result.put(child, parent);
                }
            }
        }
        return result;
    }
}
