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

	public MultipleMethodsInvocationException(String methodName, Object[] args, Set<Method> methods)
	{
		super(methodName, args);
        this.methods = methods;
	}

	@Override
	public String toString()
	{
		return "Multiple matches found for '" + getMethodName() + "' with args " + Arrays.toString(ClassUtils.getTypeSignature(getArgs())) + ", candidates are: " + methods.toString();
	}
}