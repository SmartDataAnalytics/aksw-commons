package org.aksw.commons.collections.tagmap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

public class ValidationUtils
{
    public static <T> T createValidatingProxy(Class<?> clazz, T delegate, T validate) {
        @SuppressWarnings("unchecked")
        T result = (T)Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method m, Object[] args) throws Throwable {
                        Object actual = m.invoke(delegate, args);
                        Object expected = m.invoke(validate, args);
                        
                        if(actual instanceof Stream && expected instanceof Stream) {
                        	Set<Object> as = ((Stream<?>)actual).collect(Collectors.toSet());
                        	Set<Object> es = ((Stream<?>)expected).collect(Collectors.toSet());                        	

                        	Set<Object> actualWithoutExpected = Sets.difference(as, es);
                        	Set<Object> expectedWithoutActual = Sets.difference(es, as);
                        	
                        	
                            if(!Objects.equals(as, es)) {
                            	System.err.println("Collections differ:");
                            	System.err.println("Actual (according to " + delegate + "):");
                            	for(Object a : actualWithoutExpected) {
                            		System.err.println("\t" + a);
                            	}
                            	
                            	System.err.println("Expected (according to " + validate +") :");
                            	for(Object e : expectedWithoutActual) {
                            		System.err.println("\t" + e);
                            	}                            	
                            	
                                throw new AssertionError("At invocation of: " + m + "\nWith Args: " + Arrays.toString(args) + "\nActual: " + as + "\nExpected: " + es);
                            }

                            actual = as.stream();
                            expected = es.stream();
                        } else {	                        
	                        if(!Objects.equals(actual, expected)) {
	                            throw new AssertionError("At invocation of: " + m + "\nWith Args: " + Arrays.toString(args) + "\nActual (" + delegate.getClass() + "): " + actual + "\nExpected (" + validate.getClass() + "): " + expected);
	                        }
                        }
                        
                        return actual;
                    }
                });
        return result;
    }
}
