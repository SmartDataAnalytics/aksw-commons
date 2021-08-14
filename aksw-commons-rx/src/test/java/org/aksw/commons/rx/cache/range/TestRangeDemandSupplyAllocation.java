package org.aksw.commons.rx.cache.range;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.aksw.commons.util.range.RangeUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class TestRangeDemandSupplyAllocation {

    @Test
    public void test1() {
        NavigableMap<Long, Long> supply = new TreeMap<>();
        supply.put(10l, 20l);
        supply.put(30l, 40l);
        supply.put(50l, 60l);

        RangeSet<Long> demand = TreeRangeSet.create();
        demand.add(Range.closedOpen(0l, 10l));
        demand.add(Range.closedOpen(20l, 30l));
        demand.add(Range.closedOpen(40l, 50l));
        demand.add(Range.closedOpen(60l, 100l));

        NavigableMap<Long, Long> actual = RangeUtils.scheduleRangeSupply(supply, demand, 5, 1000);
        Map<Long, Long> expected = ImmutableMap.<Long, Long>builder()
                .put(0l, 10l)
                .put(20l, 30l)
                .put(40l, 50l)
                .put(60l, 100l)
                .build();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test2() {
        NavigableMap<Long, Long> supply = new TreeMap<>();
        supply.put(10l, 20l);
        supply.put(30l, 40l);
        supply.put(50l, 60l);

        RangeSet<Long> demand = TreeRangeSet.create();
//        demand.add(Range.closedOpen(0l, 100l));
        demand.add(Range.closedOpen(0l, 10l));
        demand.add(Range.closedOpen(20l, 30l));
        demand.add(Range.closedOpen(40l, 50l));
        demand.add(Range.closedOpen(60l, 100l));

        NavigableMap<Long, Long> actual = RangeUtils.scheduleRangeSupply(supply, demand, 10, 1000);
        Map<Long, Long> expected = ImmutableMap.<Long, Long>builder().put(0l, 100l).build();
        Assert.assertEquals(expected, actual);
    }
}
