package org.aksw.commons.rx.cache.range;

import java.text.DecimalFormat;

import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetUnionTests {

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
		
		System.out.println("Complement: " + RangeSetUtils.complement(odd, Range.all()));
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
