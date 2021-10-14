package org.aksw.commons.collections;

public class PolaritySetImmutableTS
    extends PolaritySetTests
{
    public PolaritySetImmutableTS() {
        super(PolaritySet::createIntersectionView, PolaritySet::createUnionView);
    }
}
