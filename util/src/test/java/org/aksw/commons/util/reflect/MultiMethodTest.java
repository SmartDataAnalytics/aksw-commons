package org.aksw.commons.util.reflect;


import org.junit.Test;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class MultiMethodTest {
    class A {}
    class B extends A {}
    class C extends B {}
    class D extends B {}

    public static void foo(Object x) { System.out.println("O"); }
    public static void foo(A x) { System.out.println("A"); }
    public static void foo(B x) { System.out.println("B"); }
    public static void foo(C x) { System.out.println("C"); }
    public static void foo(D x) { System.out.println("D"); }

    @Test
    public void test() {
        MultiMethod.invokeStatic(MultiMethodTest.class, "foo", new D());
    }
}
