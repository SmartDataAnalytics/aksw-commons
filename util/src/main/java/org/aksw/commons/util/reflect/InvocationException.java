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
	private Class<?>[] types;

	public InvocationException(String methodName, Class<?>[] types)
	{
		this.types = types;
        this.methodName = methodName;
	}

    public String getMethodName()
    {
        return methodName;
    }


	public Object[] getTypes()
	{
		return types;
	}

	@Override
	public String toString()
	{
		return "InvocationException [types=" + java.util.Arrays.toString(types) + "]";
	}
}
