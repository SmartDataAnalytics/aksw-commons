package org.aksw.commons.util.reflect;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class MultipleMethodsInvocationException
	extends InvocationException
{
	public MultipleMethodsInvocationException(Object[] args)
	{
		super(args);
	}

	@Override
	public String toString()
	{
		return "Multiple matching methods found.";
	}
}