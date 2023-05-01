package org.aksw.commons.util.range;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;


public class TestRangeMapUtils {
    @Test
    public void testMerge() {
        RangeMap<Integer, Set<String>> rm = TreeRangeMap.create();
        Map<Range<Integer>, Set<String>> m = rm.asMapOfRanges();

        RangeMapUtils.merge(rm, Range.closedOpen(0, 10), "this", HashSet::new);
        RangeMapUtils.merge(rm, Range.closedOpen(1, 11), "is-a", HashSet::new);
        RangeMapUtils.merge(rm, Range.closedOpen(2, 8), "test", HashSet::new);

        // System.out.println(rm);
        Assert.assertEquals(m.get(Range.closedOpen(0, 1)), Set.of("this"));
        Assert.assertEquals(m.get(Range.closedOpen(1, 2)), Set.of("this", "is-a"));
        Assert.assertEquals(m.get(Range.closedOpen(2, 8)), Set.of("this", "is-a", "test"));
        Assert.assertEquals(m.get(Range.closedOpen(8, 10)), Set.of("this", "is-a"));
        Assert.assertEquals(m.get(Range.closedOpen(10, 11)), Set.of("is-a"));
    }
}
