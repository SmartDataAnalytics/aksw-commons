package org.aksw.commons.collections;

public class PolaritySetMutableTS
    extends PolaritySetTests
{
    public PolaritySetMutableTS() {
        super(PolaritySet::stateIntersect, PolaritySet::stateUnion);
    }
}
