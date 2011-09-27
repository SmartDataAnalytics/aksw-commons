package org.aksw.commons;

import com.hp.hpl.jena.ontology.OntModel;
import org.aksw.commons.jena.ClassIndexer;
import org.aksw.commons.jena.impl.OntologyCache;
import org.junit.Test;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
public class TestClassIndexer {

    @Test
    public void test() {
        OntModel m = new OntologyCache("/tmp/").loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/penn.owl");
        OntModel m1 = new OntologyCache("/tmp/").loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/system.owl");
        m.addSubModel(m1);
        //OntModel m1 = new OntologyCache("/tmp/").loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/stanford.owl");
        //OntModel m2 = new OntologyCache("/tmp/").loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/system.owl");
        new ClassIndexer().index(m);
    }

}
