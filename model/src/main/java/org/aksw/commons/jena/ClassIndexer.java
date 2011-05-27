package org.aksw.commons.jena;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.nlp2rdf.oliaconnector.impl.OntologyCache;
import org.slf4j.LoggerFactory;

import java.lang.String;import java.util.ArrayList;import java.util.HashMap;import java.util.HashSet;import java.util.List;import java.util.Map;import java.util.Set;


/**
 * Indexes an Ontology
 * skips complex classes per default, this does not affect the hierarchy outcome
 */
public class ClassIndexer {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ClassIndexer.class);

    //Options
    private boolean copyLabels = true;
    private boolean copyComments = true;
    private String language = null;

    //Not implemented
    private Map<String, String> transform = new HashMap<String, String>();
    //Not implemented
    private Set<String> remove = new HashSet<String>();

    //internal variables
    private Map<String, OntModel> classUriToClassHierarchy = new HashMap<String, OntModel>();

    public ClassIndexer() {}

    public void index(OntModel from) {
        Set<OntClass> classes = from.listClasses().toSet();
        int i = 0;
        for (OntClass cl : classes) {
            Tree t = new Tree(cl);
            classUriToClassHierarchy.put(cl.getURI(), t.toModel());
        }

    }

    /**
     * @param classUri
     * @return a filled OntModel with all superclasses of classUri or null, if no class is found
     */
    public OntModel getHierarchyForClassURI(String classUri) {
        return classUriToClassHierarchy.get(classUri);
    }

    /**
     * transforms namespaces
     *
     * @param in
     * @return
     */
    private String transformNamespace(String in) {
        String ret = in;
        for (String s : transform.keySet()) {
            if (in.startsWith(s)) {
                return in.replace(s, transform.get(s));

            }
        }
        return ret;
    }

    /**
     * filters out certain namespaces
     *
     * @param s
     * @return
     */
    private boolean filterNamespace(String s) {
        for (String prefix : remove) {
            if (s.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    public boolean isCopyLabels() {
        return copyLabels;
    }

    public void setCopyLabels(boolean copyLabels) {
        this.copyLabels = copyLabels;
    }

    public boolean isCopyComments() {
        return copyComments;
    }

    public void setCopyComments(boolean copyComments) {
        this.copyComments = copyComments;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * A simple Helper Class to convert the hierarchy
     */
    private class Tree {
        final String uri;
        List<Tree> parents;
        final String label;
        final String comment;

        public Tree(OntClass me) {
            this.uri = me.getURI();
            label = me.getLabel(language);
            comment = me.getComment(language);
            parents = new ArrayList<Tree>();

            Set<OntClass> superClasses = me.listSuperClasses(true).toSet();
            for (OntClass s : superClasses) {
                //this is were complex classes are skipped
                if (s.isAnon()) {
                    continue;
                }
                log.trace(s.toString());
                parents.add(new Tree(s));
            }
        }

        public OntModel toModel() {
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
            OntClass me = model.createClass(uri);
            if (copyLabels && label != null) {
                me.addLabel(label, language);
            }
            if (copyComments && comment != null) {
                me.addComment(comment, language);
            }
            for (Tree p : parents) {
                OntClass superClass = model.createClass(p.uri);
                me.addSuperClass(superClass);
                model.add(p.toModel());
            }
            return model;
        }
    }

}

/**
 public void expandSuperAndCopy(String originalClassUri) {

 String newClassUri = transform(originalClassUri);
 if (isRemove(originalClassUri) || isRemove(newClassUri)) {
 return;
 }


 // create initial classes
 OntClass toClass = toModel.createClass(newClassUri);
 OntClass fromClass = fromModel.getOntClass(originalClassUri);

 if(toClass==null || fromClass == null){
 logger.error("null occured in fromClass "+originalClassUri+" but retrieving yielded: "+fromClass );
 return;
 }

 //System.out.println("begin");
 //for(OntClass cltest: fromModel.listClasses().toSet()){
 //  System.out.println(cltest.getURI());
 // System.out.println(cltest.getClass().getSimpleName());
 //}
 //System.out.println("end");

 if (copyLabelsAndComments ) {
 String tmp = null;

 if((tmp=fromClass.getLabel(null))!=null) {toClass.setLabel(tmp, null);}
 //			System.out.println(fromClass.getURI()+"has label "+tmp);

 if((tmp=fromClass.getComment(null))!=null) {toClass.setComment(tmp, null);}
 //			System.out.println(fromClass.getURI()+"has comment "+tmp);
 }

 // get the superclasses
 Set<OntClass> fromSuperclasses = fromClass.listSuperClasses(true).toSet();

 for (OntClass fromSuperclass : fromSuperclasses) {
 String newFromSuperclassUri = transform(fromSuperclass.getURI());
 if (isRemove(fromSuperclass.getURI()) || isRemove(newFromSuperclassUri)) {
 continue;
 }
 if(fromSuperclass.isAnon()){
 continue;
 }

 OntClass toSuperclass = toModel.createClass(newFromSuperclassUri);
 toClass.addSuperClass(toSuperclass);

 if (copyLabelsAndComments) {
 String tmp = null;
 if((tmp=fromSuperclass.getLabel(null))!=null) {toSuperclass.setLabel(tmp, null);}
 //				System.out.println(fromSuperclass.getURI()+"has label "+tmp);

 if((tmp=fromSuperclass.getComment(null))!=null) {toSuperclass.setComment(tmp, null);}
 //				System.out.println(fromSuperclass.getURI()+"has comment "+tmp);
 }
 // System.out.println(fromSuperclass);
 expandSuperAndCopy(fromSuperclass.getURI());
 }

 }     **/