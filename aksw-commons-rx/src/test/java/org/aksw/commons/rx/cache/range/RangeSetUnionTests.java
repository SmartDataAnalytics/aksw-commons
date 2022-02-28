package org.aksw.commons.rx.cache.range;

import java.text.DecimalFormat;

import org.aksw.commons.collection.rangeset.RangeSetOps;
import org.aksw.commons.collection.rangeset.RangeSetUnion;
import org.aksw.commons.collection.rangeset.RangeSetUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class RangeSetUnionTests {

    @Test
    public void testOverlap() {

        RangeSet<Integer> odd = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(0, 10))
                .add(Range.closedOpen(30, 40))
                .add(Range.closedOpen(50, 100))
                .build();

        RangeSet<Integer> even = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(0, 10))
                .add(Range.closedOpen(20, 50)) // encloses [30, 40)
                .add(Range.closedOpen(60, 90)) // enclosed by [50, 100)
                .build();

        RangeSet<Integer> view = RangeSetUnion.create(odd, even);

        System.out.println("Overlap: " + view); // Expceted: [[0..10), [20..100)]
    }


    @Test
    public void testGenericComplement() {
    	RangeSet<Long> c = RangeSetOps.union(TreeRangeSet.<Long>create(), TreeRangeSet.create()).complement();

    	Range<Long> first = c.asRanges().iterator().next();
    	Assert.assertEquals(first, Range.all());
    
    	Assert.assertEquals(c.span(), Range.all());
    }
    
    
    @Test
    public void testFlat() {

        DecimalFormat df = new DecimalFormat();
        df.setMinimumIntegerDigits(8);
        df.setGroupingUsed(false);
        System.out.println("Format test: " + df.format(1));

        RangeSet<Integer> flat = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(1, 2))
                .add(Range.closedOpen(2, 3))
                .add(Range.closedOpen(3, 4))
                .build();

        System.out.println("Flat: " + flat);
    }

    @Test
    public void testComplement() {
        RangeSet<Integer> odd = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(1, 2))
                .add(Range.closedOpen(3, 4))
                .build();

        System.out.println("Complement: " + odd.complement());
        // System.out.println(odd.complement().subRangeSet(Range.closedOpen(1, 3)));
    }

    @Test
    public void testDifference() {
        RangeSet<Integer> a = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(0, 10))
                .add(Range.closedOpen(20, 30))
                .build();

        RangeSet<Integer> b = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(5, 10))
                .add(Range.closedOpen(15, 25))
                .build();

        System.out.println("Difference (a, b): " + RangeSetUtils.difference(a, b));
        System.out.println("Difference (b, a): " + RangeSetUtils.difference(b, a));
    }

    @Test
    public void test() {

        RangeSet<Integer> odd = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(1, 2))
                .add(Range.closedOpen(3, 4))
                .add(Range.closedOpen(5, 6))
                .build();

        RangeSet<Integer> even = ImmutableRangeSet.<Integer>builder()
                .add(Range.closedOpen(2, 3))
                // .add(Range.closedOpen(4, 5))
                .add(Range.closedOpen(6, 7))
                .build();

        RangeSet<Integer> view = RangeSetUnion.create(odd, even);

        // view = view.subRangeSet(Range.closedOpen(3, 6));


        System.out.println(view.asRanges());
        System.out.println(view.asDescendingSetOfRanges());
//		System.out.println(view.asDescendingSetOfRanges());

        System.out.println(view.rangeContaining(3)); // [1..4)

        System.out.println(view.rangeContaining(4)); // null

        System.out.println(view.rangeContaining(6)); // [5..7)

        System.out.println(even.complement());
        System.out.println(odd.complement());

        System.out.println(view.complement());
    }
}
