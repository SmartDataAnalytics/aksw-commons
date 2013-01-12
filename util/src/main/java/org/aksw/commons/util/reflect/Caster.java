package org.aksw.commons.util.reflect;


import java.lang.reflect.Method;

/**
 * Utility class for common casts.
 * TODO Make this more configurable.
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/17/11
 *         Time: 5:31 PM
 */
public class Caster {

    /*
    public static Object cast(Object value, Class<?> targetClass)
    {
        Method method = MultiMethod.findInvocationMethod(Caster.class, targetClass, value.getClass());
        return method.invoke(null, value);
    }*/


//	public static NodeValue tryCast(Object value, String targetType) {
//		
//	}
	

    public static Object tryCast(Object value, Class<?> targetClass)
    {
        try {
            Method method = MultiMethod.findInvocationMethod(Caster.class, targetClass, value.getClass());

            return method.invoke(null, value);
        } catch (Exception e) {
            if(e.getCause() instanceof NumberFormatException) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(tryCast("123.4", Double.class));
        System.out.println(tryCast(123.4, String.class));
        System.out.println(tryCast("test", Double.class));
    }

    public static String toString(Object o) {
        return o.toString();
    }


	public static Boolean toBoolean(String a)
	{
		return Boolean.parseBoolean(a);
	}


	public static Integer toInteger(Long a)
	{
		return a.intValue();
	}

	public static Integer toInteger(Float a)
	{
		return a.intValue();
	}

	public static Integer toInteger(Double a)
	{
		return a.intValue();
	}

	public static Integer toInteger(String a)
	{
		return Integer.parseInt(a);
	}


	public static Long toLong(Integer a)
	{
		return a.longValue();
	}

	public static Long toLong(Float a)
	{
		return a.longValue();
	}

	public static Long toLong(Double a)
	{
		return a.longValue();
	}

	public static Long toLong(String a)
	{
		return Long.parseLong(a);
	}



	public static Float toFloat(Long a)
	{
		return a.floatValue();
	}

	public static Float toFloat(Integer a)
	{
		return a.floatValue();
	}

	public static Float toFloat(Double a)
	{
		return a.floatValue();
	}

	public static Float toFloat(String s)
	{
		return Float.parseFloat(s);
	}



	public static Double toDouble(Integer a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(Long a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(Float a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(String a)
	{
		return Double.parseDouble(a);
	}
}
