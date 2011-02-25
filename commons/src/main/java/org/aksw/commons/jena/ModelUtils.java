package org.aksw.commons.jena;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.aksw.commons.util.collections.MultiMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Claus Stadler
 * Date: 2/25/11
 * Time: 12:54 PM
 */
public class ModelUtils {

    /**
     * Extracts a mapping childClass -> parentClass from a given model.
     * You can use TransitiveClosure.transitiveClosure for "inferring" the whole hierarchy.
     *
     * @param model
     * @return
     */
    public static Map<Resource, Set<Resource>> extractDirectSuperClassMap(Model model) {
        Map<Resource, Set<Resource>> result = new HashMap<Resource, Set<Resource>>();

        StmtIterator it = model.listStatements(null, RDFS.subClassOf, (RDFNode)null);
        while (it.hasNext()) {
            Statement stmt = it.next();

            // Skip "invalid" triples
            if(!(stmt.getObject() instanceof Resource))
                continue;

            MultiMaps.put(result, stmt.getSubject(), (Resource)stmt.getObject());
        }
        it.close();

        return result;
    }
}
