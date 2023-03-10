package org.aksw.commons.algebra.allen;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;


public class TestAllenIntervalAlgebra {

    /** Sanity check the inverses */
    public static <T extends Comparable<T>> AllenRelation test(Range<T> x, Range<T> y) {
        AllenRelation result = AllenRelations.compute(x, y);

        AllenRelation inv = AllenRelations.compute(y, x);
        AllenRelation invResult = result.invert();
        Assert.assertEquals(inv, invResult);

        return result;
    }

    @Test
    public void test_before_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.BEFORE);
        AllenRelation expected = test(Range.closedOpen(0, 4), Range.closedOpen(6, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_starts_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.STARTS);
        AllenRelation expected = test(Range.closedOpen(0, 5), Range.closedOpen(0, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_meets_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.MEETS);
        AllenRelation expected = test(Range.closedOpen(0, 5), Range.closedOpen(5, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_overlaps_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.OVERLAPS);
        AllenRelation expected = test(Range.closedOpen(-1, 5), Range.closedOpen(0, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_during_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.DURING);
        AllenRelation expected = test(Range.closedOpen(1, 5), Range.closedOpen(0, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_finishes_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.FINISHES);
        AllenRelation expected = test(Range.closedOpen(1, 10), Range.closedOpen(0, 10));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_equals_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.EQUALS);
        AllenRelation expected = test(Range.closedOpen(0, 10), Range.closedOpen(0, 10));
        Assert.assertEquals(expected, actual);
    }

    /* Inverses */

    @Test
    public void test_after_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.AFTER);
        AllenRelation expected = test(Range.closedOpen(6, 10), Range.closedOpen(0, 4));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_startedby_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.STARTEDBY);
        AllenRelation expected = test(Range.closedOpen(0, 10), Range.closedOpen(0, 5));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_metby_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.METBY);
        AllenRelation expected = test(Range.closedOpen(5, 10), Range.closedOpen(0, 5));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_overlappedby_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.OVERLAPPEDBY);
        AllenRelation expected = test(Range.closedOpen(0, 10), Range.closedOpen(-1, 5));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_contains_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.CONTAINS);
        AllenRelation expected = test(Range.closedOpen(0, 10), Range.closedOpen(1, 5));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test_finishedby_01() {
        AllenRelation actual = AllenRelation.of(AllenConstants.FINISHEDBY);
        AllenRelation expected = test(Range.closedOpen(0, 10), Range.closedOpen(1, 10));
        Assert.assertEquals(expected, actual);
    }
}
