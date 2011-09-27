package org.aksw.commons.util.reflect;

/**
 * @author Claus Stadler
 * 
 * Date: 6/3/11
 */
abstract class InvocationException
	extends RuntimeException
{
    private String methodName;
	private Object[] args;

	public InvocationException(String methodName, Object[] args)
	{
		this.args = args;
        this.methodName = methodName;
	}

    public String getMethodName()
    {
        return methodName;
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
