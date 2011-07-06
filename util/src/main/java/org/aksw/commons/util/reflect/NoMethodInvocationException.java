package org.aksw.commons.util.reflect;


import org.aksw.commons.util.strings.StringUtils;

import java.util.Arrays;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class NoMethodInvocationException
	extends InvocationException
{
	public NoMethodInvocationException(String methodName, Object[] args)
	{
		super(methodName, args);
	}

	@Override
	public String toString()
	{
		return "No method '" + getMethodName() + "' found for arguments " + Arrays.toString(ClassUtils.getTypeSignature(getArgs()));
	}
}
