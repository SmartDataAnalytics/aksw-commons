package org.aksw.commons.util.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class MultipleMethodsInvocationException
	extends InvocationException
{
    private Set<Method> methods;

	public MultipleMethodsInvocationException(String methodName, Class<?>[] types, Set<Method> methods)
	{
		super(methodName, types);
        this.methods = methods;
	}

	@Override
	public String toString()
	{
		return "Multiple matches found for '" + getMethodName() + "' with types " + Arrays.toString(getTypes()) + ", candidates are: " + methods.toString();
	}
}