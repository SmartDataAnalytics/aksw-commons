package org.aksw.commons;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.jena.Constants;
import org.aksw.commons.jena.StringConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  20.09.2010 19:19:50
 */
public class TestToString {
    URL wine;

    @Before
    public void init(){
          wine = this.getClass().getClassLoader().getResource("wine.rdf");
    }

    public void informallyTestModelConversions(){
        OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        m.read(wine.toString());
        System.out.println("Triples in pellet: "+m.size());
        System.out.println("Triples in raw model: "+m.getRawModel().size());
        System.out.println("Triples in base model: "+m.getBaseModel().size());

        System.out.println("Conversion");

        OntModel n;

        //n = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        //n.add(m);
        //System.out.println("Triples in pellet: "+n.size());

         n = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        n.add(m.getRawModel());
        System.out.println("Triples in raw model: "+n.size());

        n = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        n.add(m.getBaseModel());
        System.out.println("Triples in base model: "+n.size());

    }

    @Test
     public void testToStringProper(){
        OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        m.read(wine.toString());

        StringConverter sc = new StringConverter(m);
        sc.setUseProperStringSerialization(true);

        long now = System.currentTimeMillis();
        String s = sc.toString(Constants.N_TRIPLE);
        long needed = System.currentTimeMillis()-now;
        System.out.println("chars : "+s.length());

        OntModel target = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());

        target.read(bais, "",Constants.N_TRIPLE);

         System.out.println("Before: "+m.size());
        System.out.println("After: "+target.size());
        System.out.println("Needed: "+needed);

        Assert.assertTrue(target.size()==3971);
        Assert.assertTrue(m.size()==3971);





    }

    @Test
     public void testToStringFileBased(){
        OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        m.read(wine.toString());

        StringConverter sc = new StringConverter(m);
        sc.setUseProperStringSerialization(false);

        long now = System.currentTimeMillis();
        String s = sc.toString(Constants.N_TRIPLE);
        long needed = System.currentTimeMillis()-now;
        System.out.println("chars : "+s.length());

        OntModel target = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

         ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());

        target.read(bais, "",Constants.N_TRIPLE);

        System.out.println("Before: "+m.size());
        System.out.println("After: "+target.size());
        System.out.println("Needed: "+needed);

        Assert.assertTrue(target.size()==3971);
        Assert.assertTrue(m.size()==3971);



    }


    

}
