package org.aksw.commons.util.reflect;


import java.util.Arrays;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class NoMethodInvocationException
	extends InvocationException
{
	public NoMethodInvocationException(String methodName, Class<?>[] types)
	{
		super(methodName, types);
	}

	@Override
	public String toString()
	{
		return "No method '" + getMethodName() + "' found for argument types " + Arrays.toString(getTypes());
	}
}
