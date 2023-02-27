package org.aksw.commons.util.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;

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

    /**
     * Factorize the argument expression against the state of this object.
     * Adds all sub expression to the varToExpr map.
     */
    public E factorize(E expr) {
        E result = exprFactorizer.factorize(expr, varToExpr, nextVar);
        return result;
    }

    /** Call {@link #factorize(Object)} and add the result as a new root */
    public E addRoot(E expr) {
        E newRoot = factorize(expr);
        roots.add(newRoot);
        return newRoot;
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
            collapse(root, mm);
        }
    }

    // TODO add prune method - remove all entries not reachable from a root

    /**
     * Merge and remove all non-root nodes having only a single parent into the parent.
     */
    protected void collapse(E expr, Multimap<V, V> mm) {
        if (exprOps.isVar(expr)) {
            V exprVar = exprOps.asVar(expr);
            E exprDef = getExpr(exprVar);
            if (exprDef != null) {
                Set<V> subExprVars = exprOps.varsMentioned(exprDef);
                for (V subExprVar : subExprVars) {
                    E subExprDef = exprOps.varToExpr(subExprVar);
                    if (subExprDef != null) {
                        collapse(subExprDef, mm);
                    }
                }

                E newDef = ExprOps.replace(exprOps, exprDef, ev -> {
                    E r = null;
                    if (exprOps.isVar(ev)) {
                        V v = exprOps.asVar(ev);
                        if (!roots.contains(ev) && mm.get(v).size() == 1) {
                            r = getExpr(v);
                            remove(v);
                        }
                    }
                    if (r == null) {
                        r = ev;
                    }
                    return r;
                });

                remove(exprVar);
                varToExpr.put(exprVar, newDef);
            }
        }
    }

    /**
     * Recursively expand a given expression against the dag by means of
     * substituting variables with available definitions from the dag.
     * Does not cause any change in the dag.
     */
    public static <E, V> E expand(GenericDag<E, V> dag, E root) {
        ExprOps<E, V> exprOps = dag.getExprOps();
        E result = ExprOps.replace(dag.getExprOps(), root, e -> {
            E r = e;
            if (exprOps.isVar(e)) {
                V var = exprOps.asVar(e);
                E def = dag.getExpr(var);
                if (def != null) {
                    r = expand(dag, def);
                }
            }
            return r;
        });
        return result;
    }

    public static <E, V> Map<V, E> getSortedDependencies(GenericDag<E, V> dag) {
        return getSortedDependencies(dag, dag.getRoots());
    }

    /**
     * For the given set of roots return an ordered set of expression such that
     * if any expression y depends on x then x comes earlier in the list.
     *
     * TODO Clarify whether roots that are variables need to be added or not...
     * // Root nodes that are variables but do not have a definition are added as (root, null) entries.
     */
    public static <E, V> Map<V, E> getSortedDependencies(GenericDag<E, V> dag, Collection<E> roots) {
        ExprOps<E, V> exprOps = dag.getExprOps();
        Map<V, E> result = new LinkedHashMap<>();
        for (E root : roots) {
            getSortedDependencies(dag, root, result);
            if (exprOps.isVar(root)) {
                V rootVar = exprOps.asVar(root);
                if (!result.keySet().contains(rootVar)) {
                    result.put(rootVar, null);
                }
            }
        }
        return result;
    }

    public static <E, V> void getSortedDependencies(GenericDag<E, V> dag, E node, Map<V, E> acc) {
        ExprOps<E, V> exprOps = dag.getExprOps();
        Set<V> varsMentioned = exprOps.varsMentioned(node);
        for (V var : varsMentioned) {
            E def = dag.getExpr(var);
            if (def != null) {
                getSortedDependencies(dag, def, acc);
                if (!acc.containsKey(var)) {
                    acc.put(var, def);
                }
            }
        }
    }

    public static <E, V> Set<V> getUndefinedVars(GenericDag<E, V> dag) {
        return getUndefinedVars(dag, dag.getRoots());
    }

    /** Return the set of variables that do not have a definition in the dag */
    public static <E, V> Set<V> getUndefinedVars(GenericDag<E, V> dag, Set<E> roots) {
        ExprOps<E, V> exprOps = dag.getExprOps();
        SuccessorsFunction<E> successors = createSuccessorFunction(dag);
        Set<V> result = Streams.stream(Traverser.forTree(successors).depthFirstPostOrder(roots))
            .filter(exprOps::isVar)
            .map(exprOps::asVar)
            .filter(v -> dag.getExpr(v) == null)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    /** Successor function for use with guava's traverser */
    public static <E, V> SuccessorsFunction<E> createSuccessorFunction(GenericDag<E, V> dag) {
        ExprOps<E, V> exprOps = dag.getExprOps();
        return e -> {
            List<E> r;
            // If the argument is a var then resolve and recurse into the resolved expression
            if (exprOps.isVar(e)) {
                V var = exprOps.asVar(e);
                E def = dag.getExpr(var);
                r = def != null
                    ? Collections.singletonList(def)
                    : Collections.emptyList();
            } else {
                // Descend into the immediate children of the expression
                r = exprOps.getSubExprs(e);
            }
            return r;
        };
    }



    // TODO Wrap this up as an iterator similar to guava's traverser
    // This is depth first post order
    public static <E, V> void depthFirstTraverse(GenericDag<E, V> dag, E parent, int childIdx, E child, ExprFilter<E> isBlocked, Consumer<E> visitor) {
        if (!isBlocked.test(parent, childIdx, child)) {
            ExprOps<E, V> exprOps = dag.getExprOps();

            // Descend
            // If the argument is a var then resolve it to the op and keep recursing
            if (exprOps.isVar(child)) {
                V var = exprOps.asVar(child);
                E def = dag.getExpr(var);
                if (def != null) {
                    depthFirstTraverse(dag, child, 0, def, isBlocked, visitor);
                    // We don't visit variable nodes if we could resolve them
                } else {
                    // Visit the node itself
                    visitor.accept(child);
                }
            } else {
                // Descend into the immediate children of the expression
                List<E> subExprs = exprOps.getSubExprs(child);
                for (int i = 0; i < subExprs.size(); ++i) {
                    E subExpr = subExprs.get(i);
                    depthFirstTraverse(dag, child, i, subExpr, isBlocked, visitor);
                }

                // Visit the node itself
                visitor.accept(child);
            }
        }
    }

}
