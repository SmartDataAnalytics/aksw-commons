package org.aksw.commons.util.reflect;


/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class NoMethodInvocationException
	extends InvocationException
{
	public NoMethodInvocationException(Object[] args)
	{
		super(args);
	}

	@Override
	public String toString()
	{
		return "No matching method found.";
	}
}
