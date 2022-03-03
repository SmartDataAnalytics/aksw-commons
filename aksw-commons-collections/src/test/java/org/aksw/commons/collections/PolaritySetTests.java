package org.aksw.commons.collections;

import java.util.function.BinaryOperator;

import org.junit.Test;

import junit.framework.Assert;

public abstract class PolaritySetTests {

    interface PolaritySetBinaryOp<T>
        extends BinaryOperator<PolaritySet<T>> {}

    protected PolaritySetBinaryOp<String> intersect;
    protected PolaritySetBinaryOp<String> union;

    protected PolaritySetTests(
            PolaritySetBinaryOp<String> intersect,
            PolaritySetBinaryOp<String> union) {
        this.intersect = intersect;
        this.union = union;
    }

    /* {not a} intersect {a}= {} */
    @Test
    public void test1() {
        PolaritySetTests.testSymmetric(
                PolaritySet.create(true),
                PolaritySet.create(false, "a"),
                PolaritySet.create(true, "a"),
                intersect);
    }

    /* not{a} intersect {b}= {b} */
    @Test
    public void test2() {
        testSymmetric(
                PolaritySet.create(true, "b"),
                PolaritySet.create(false, "a"),
                PolaritySet.create(true, "b"),
                intersect);
    }


    /* {} union { a, b ,c } = { a, b, c} */
    @Test
    public void test3() {
        testSymmetric(
                PolaritySet.create(true, "a", "b", "c"),
                PolaritySet.create(true),
                PolaritySet.create(true, "a", "b", "c"),
                union);
    }

    /* not{} union { a, b ,c } = not {} */
    @Test
    public void test4() {
        testSymmetric(
                PolaritySet.create(false),
                PolaritySet.create(false),
                PolaritySet.create(true, "a", "b", "c"),
                union);
    }

    public static <T> void testSymmetric(
            PolaritySet<T> expected,
            PolaritySet<T> a,
            PolaritySet<T> b,
            BinaryOperator<PolaritySet<T>> op) {
        Assert.assertEquals(expected, op.apply(a.clone(), b));
        Assert.assertEquals(expected, op.apply(b.clone(), a));
    }

}
