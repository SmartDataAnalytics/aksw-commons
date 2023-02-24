package org.aksw.commons.util.algebra;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class GenericDag<E, V> {
    protected ExprOps<E, V> exprOps;
    protected ExprFilter<E> isBlocker;
    protected GenericFactorizer<E, V> exprFactorizer;
    protected Supplier<V> nextVar;
    protected BiMap<V, E> varToExpr = HashBiMap.create();
    protected Set<E> roots = new LinkedHashSet<>();

    public GenericDag(ExprOps<E, V> exprOps, Supplier<V> nextVar, ExprFilter<E> isBlocker) {
        super();
        this.exprOps = exprOps;
        this.isBlocker = isBlocker;
        this.exprFactorizer = new GenericFactorizer<>(exprOps, isBlocker);
        this.nextVar = nextVar;
    }

//    public GenericDag(GenericFactorizer<E, V> exprFactorizer, Supplier<V> nextVar) {
//        super();
//        this.exprFactorizer = exprFactorizer;
//        this.nextVar = nextVar;
//    }

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

    public E getExpr(V var) {
        E result = varToExpr.get(var);
        return result;
    }

    public V getVar(E expr) {
        V result = varToExpr.inverse().get(expr);
        return result;
    }

    public E remove(V var) {
        return varToExpr.remove(var);
    }

    public Multimap<V, V> getChildToParent() {
        Multimap<V, V> result = HashMultimap.create();
        for (Entry<V, E> e : varToExpr.entrySet()) {
            V parent = e.getKey();
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

    public Set<V> getVarsWithMultipleParents() {
        Set<V> result = new LinkedHashSet<>();
        Map<V, Collection<V>> map = getChildToParent().asMap();
        for (Entry<V, Collection<V>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Return a new DAG where nodes with just a single parent are collapsed */
    public void collapse() {
        Multimap<V, V> mm = getChildToParent();

        for (E root : roots) {
            collapse(null, 0, root, mm);
        }
    }

    // TODO add prune method - remove all entries not reachable from a root

    /**
     * Merge and remove all non-root nodes having only a single parent into the parent.
     */
    protected void collapse(E parent, int childIdx, E childExpr, Multimap<V, V> mm) {
        if (isBlocker == null || !isBlocker.test(parent, childIdx, childExpr)) {
            Set<V> mentionedVars = ExprOps.varsMentioned(exprOps, childExpr);
            int i = 0;
            for (V mentionedVar : mentionedVars) {
                E subExpr = getExpr(mentionedVar);
                collapse(childExpr, i, subExpr, mm);
                ++i;
            }
        }
        // Do not collapse roots
        if (!roots.contains(childExpr)) {
            V childVar = getVar(childExpr);
            Collection<V> parents = mm.get(childVar);
            if (parents.size() == 1) {
                V parentVar = parents.iterator().next();
                E parentDefBefore = getExpr(parentVar);
                E childVarExpr = exprOps.varToExpr(childVar);
                E parentDefAfter = ExprOps.replace(exprOps, parentDefBefore, e -> e.equals(childVarExpr) ? childExpr : e);
                remove(childVar);
                remove(parentVar);
                varToExpr.put(parentVar, parentDefAfter);
            }
        }
    }
}
