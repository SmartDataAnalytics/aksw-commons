package org.aksw.commons.collections.tracker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.collections.diff.SetDiff;
import org.junit.Assert;
import org.junit.Test;

public class TrackingSetTest {

    @Test
    public void test() {
        Set<String> base = new HashSet<>(Arrays.asList("anne", "bob", "cesar"));

        TrackingSet<String> trackingSet = new TrackingSet<>(base);
        
        trackingSet.add("doreen");
        trackingSet.remove("bob");
        
        trackingSet.remove("anne");
        trackingSet.add("anne");
        
        Set<String> expectedSet = new HashSet<>(Arrays.asList("anne", "cesar", "doreen"));
        SetDiff<String> expectedDiff = new SetDiff<>(new HashSet<>(Arrays.asList("doreen")), new HashSet<>(Arrays.asList("bob")), null);  

        Assert.assertEquals(expectedSet, trackingSet);
        Assert.assertEquals(expectedDiff, trackingSet.getDiff());

        // Restore the original state
        trackingSet.restore();
        
        Assert.assertEquals(base, trackingSet);
    }

}
