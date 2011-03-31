package org.aksw.commons.owlapi.reasoning.impl;

import org.semanticweb.owlapi.model.OWLClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class SubsumptionResult {
     public Set<OWLClass> superClasses = new HashSet<OWLClass>();
     public Set<OWLClass> subClasses =new HashSet<OWLClass>();
     public Set<OWLClass> equivalentClasses =new HashSet<OWLClass>();
     public Set<OWLClass> siblingClasses =new HashSet<OWLClass>();

    @Override
    public String toString() {
        return "SubsumptionResult{" + "superClasses=" + superClasses.size() + ", subClasses=" + subClasses.size() + ", equivalentClasses=" + equivalentClasses.size() + ", siblingClasses=" + siblingClasses.size() + '}';
    }
}


