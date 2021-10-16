package org.aksw.commons.util.convert;

import org.junit.Assert;
import org.junit.Test;

public class ConverterRegistryTest {

	static class A { String get() { return "a"; } }
	static class B extends A { String get() { return "b"; } };
	static class C extends B { String get() { return "c"; } };

	
	@Test
	public void test1() throws Exception {
		ConverterRegistryImpl reg = new ConverterRegistryImpl();
		reg.register(ConverterRegistryTest.class.getMethod("conv", int.class));

		Converter conv = reg.getConverter(int.class, String.class);
		Object r = conv.getFunction().apply(1);
		String actual = (String)r;		
		Assert.assertEquals("hello 1", actual);
	}

	@Test
	public void test2() throws Exception {
		ConverterRegistryImpl reg = new ConverterRegistryImpl();
		reg.register(ConverterRegistryTest.class.getMethod("conv", A.class));

		// Lookup a converter using B - this should find the converter that accepts A
		Converter conv = reg.getConverter(B.class, String.class);
		Object r = conv.getFunction().apply(new B());
		String actual = (String)r;		
		Assert.assertEquals("hello b", actual);
	}

	@Test
	public void test3() throws Exception {
		ConverterRegistryImpl reg = new ConverterRegistryImpl();
		reg.register(ConverterRegistryTest.class.getMethod("toB", int.class));

		// Lookup a converter that returns A - this should find the converter that returns B
		Converter conv = reg.getConverter(int.class, A.class);
		Object r = conv.getFunction().apply(1);
		B actual = (B)r;		
		Assert.assertEquals("b", actual.get());
	}

	
	// TODO Should this test raise a multiple candidates exception or pick a certain converter?
//	@Test
//	public void test3b() throws Exception {
//		ConverterRegistryImpl reg = new ConverterRegistryImpl();
//		reg.register(ConverterRegistryTest.class.getMethod("toB", int.class));
//		reg.register(ConverterRegistryTest.class.getMethod("toC", int.class));
//
//		// Lookup a converter that returns A - this should find the converter that returns B
//		// because B is closer to A than C
//		Converter conv = reg.getConverter(int.class, A.class);
//		Object r = conv.getFunction().apply(1);
//		B actual = (B)r;		
//		Assert.assertEquals("b", actual.get());
//	}

	
	
	public static String conv(int i) {
		return "hello " + i;
	}
	
	public static String conv(A a) {
		return "hello " + a.get();
	}

	public static A toA(int i) {
		return new A();
	}

	public static B toB(int i) {
		return new B();
	}

	public static C toC(int i) {
		return new C();
	}

}
