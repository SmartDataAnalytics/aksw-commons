package org.aksw.commons.sparql;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.aksw.commons.sparql.core.SparqlEndpoint;

import java.util.Collection;

/**
 * A collection of canned sparql queries.
 *
 *
 * User: raven
 * Date: 5/18/11
 * Time: 2:39 PM
 */
object CannedQueries {
    def constructBySubjects(subjects : Collection[Resource]) : String =
    {
        val filterStr = Joiner.on("> || ?s = <").join(subjects)

        val queryStr =
                "Construct {?s ?p ?o } { ?s ?p ?o . Filter(?s = <" + filterStr + "> ) . }";

        //Query query = new Query();
        //QueryFactory.parse(query, queryStr, null, Syntax.syntaxSPARQL);

        return queryStr
    }

    def execConstructBySubjects(endpoint : SparqlEndpoint, model : Model, subjects : Collection[Resource]) : Model =
    {
      if(subjects.isEmpty) {
        return model;
      }

      return endpoint.executeConstruct(constructBySubjects(subjects), model)
    }
}

/*
public class CannedQueries {
    public static String constructBySubjects(Collection<Resource> subjects)
    {
        String filterStr = Joiner.on("> || ?s = <").join(subjects);

        String queryStr =
                "Construct { ?s ?p ?o . Filter(?s = <" + filterStr + "> ) . }";

        //Query query = new Query();
        //QueryFactory.parse(query, queryStr, null, Syntax.syntaxSPARQL);

        return queryStr;
    }

    public static Model execConstructBySubjects(SparqlEndpoint endpoint, Model model, Collection<Resource> subjects)
    {
        return endpoint.executeConstruct(constructBySubjects(subjects), model);
    }
}
*/
