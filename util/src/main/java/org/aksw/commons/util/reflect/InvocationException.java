package org.aksw.commons.util.reflect;

/**
 * @author Claus Stadler
 * 
 * Date: 6/3/11
 */
abstract class InvocationException
	extends RuntimeException
{
	private Object[] args;

	public InvocationException(Object[] args)
	{
		this.args = args;
	}

	public Object[] getArgs()
	{
		return args;
	}

	@Override
	public String toString()
	{
		return "InvocationException [args=" + java.util.Arrays.toString(args) + "]";
	}
}
