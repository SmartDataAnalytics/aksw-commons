package org.aksw.commons.sparql.api.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/1/11
 *         Time: 1:12 AM
 */
public class CannedQueryUtils {
    public static Query spoTemplate() {
        return spoTemplate(Node.createVariable("s"), Node.createVariable("p"), Node.createVariable("o"));
    }

    public static Query spoTemplate(Node s, Node p, Node o)
    {
        Query query = QueryFactory.create();
        query.setQuerySelectType();

        Triple triple = new Triple(s, p, o);
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);
        query.setQueryPattern(group);

        if(s.isVariable()) {
            query.getProject().add(Var.alloc(s.getName()));
        }
        if(p.isVariable()) {
            query.getProject().add(Var.alloc(p.getName()));
        }
        if(o.isVariable()) {
            query.getProject().add(Var.alloc(o.getName()));
        }

        return query;
    }
}
