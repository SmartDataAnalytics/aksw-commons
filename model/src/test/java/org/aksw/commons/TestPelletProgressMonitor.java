package org.aksw.commons;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  22.09.2010 11:36:06
 */
public class TestPelletProgressMonitor {

     URL pizza = this.getClass().getClassLoader().getResource("pizza.owl");

    @Test
    public void test(){
        if(true)return;
        PelletOptions.USE_CLASSIFICATION_MONITOR= PelletOptions.MonitorType.NONE;
        OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        m.read(pizza.toString());
        m.listStatements();
    }
}
