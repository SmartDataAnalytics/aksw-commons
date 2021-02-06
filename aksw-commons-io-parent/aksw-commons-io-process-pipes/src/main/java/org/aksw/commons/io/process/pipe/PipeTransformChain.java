package org.aksw.commons.io.process.pipe;

public interface PipeTransformChain {
    PipeTransformChain add(PipeTransform xform);
}
