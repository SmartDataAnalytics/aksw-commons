package org.aksw.commons.owlapi.reasoning;

import org.aksw.commons.owlapi.reasoning.impl.SubsumptionResult;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public interface IncrementalClassifier {

    public SubsumptionResult getSubsumptionResult(String classUri, boolean direct);
    

}
