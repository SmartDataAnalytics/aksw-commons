package org.aksw.commons.util.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Claus Stadler
 *
 * Date: 6/3/11
 */
public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static Object forceInvoke(Method m, Object[] args)
    {
        return forceInvoke(null, m, args);
    }

    public static Object forceInvoke(Object o, Method m, Object[] args)
    {
        // FIXME Not multithreading safe
        boolean isAccessible = m.isAccessible();
        m.setAccessible(true);

		try {
            logger.trace("Invoking " + m + " on " + o);
			return m.invoke(o, args);
		} catch (Exception e) {
			//throw new RuntimeException("Invocation failed", e);
            throw new RuntimeException(e);
		} finally {
            m.setAccessible(isAccessible);
        }
    }

    /**
	 * Returns the minimum distance of two classes in an inheritance hierarchy
	 * Null if there is no distance
	 */
	public static Integer getDistance(Class<?> given, Class<?> there)
	{
		int result;
		if(there.isInterface())
			result = _getDistanceInterface(given, there, 0);
		else
			result = _getDistanceClass(given, there);

		return result == Integer.MAX_VALUE ? null : result;
	}

	private static int _getDistanceClass(Class<?> given, Class<?> there)
	{
		int distance = 0;
		do {
			if(given == there)
				return distance;

			distance += 1;
			given = given.getSuperclass();


		} while (given != null);

		return Integer.MAX_VALUE;
	}

	private static int _getDistanceInterface(Class<?> given, Class<?> there, int depth)
	{
		if(given == there)
			return depth;

		++depth;

		int result = Integer.MAX_VALUE;
		for(Class<?> item : given.getInterfaces())
			result = Math.min(result, _getDistanceInterface(item, there, depth));

		Class<?> superClass = given.getSuperclass();
		if(superClass != null)
			result = Math.min(result, _getDistanceInterface(superClass, there, depth));

		return result;
	}

    public static Class<?>[] getTypeSignature(Object[] args)
    {
        Class<?>[] result = new Class<?>[args.length];
        for(int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            result[i] = (arg == null ? null : arg.getClass());
        }

        return result;
    }

    public static Integer[] getDistance(Class<?>[] a, Class<?>[] b)
    {
        int n = Math.min(a.length, b.length);

        Integer[] result = new Integer[n];
        for(int i = 0; i < n; ++i) {
            Integer d = getDistance(a[i], b[i]);
            result[i] = d;
        }

        return  result;
    }

    /**
     *
     *
     * @param a
     * @param b
     * @return L, LE, E, GE, G, Mixed
     */
    public static Integer getRelation(Integer[] a, Integer[] b)
    {
        boolean hasGreater = false;
        boolean hasLess = false;

        for(int i = 0; i < a.length; ++i) {
            if(a[i] == null || b[i] == null) {
                //return null;
                throw new NullPointerException();
                // TODO Throw an exception or return null?
            }

            int d = a[i] - b[i];

            if (d > 0) {
                hasGreater = true;
            } else if (d < 0) {
                hasLess = true;
            }
        }

        if(hasGreater && hasLess) {
            return null;
        } else if(hasGreater) {
            return 1;
        } else if(hasLess) {
            return -1;
        }

        return 0;
    }

    /**
     * Returns all non-overridden methods for the given class.
     *
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAllNonOverriddenlMethods(Class<?> clazz)
    {
        List<Method> result = new ArrayList<Method>();

        Set<MethodSignature> signatures = new HashSet<MethodSignature>();
        while(clazz != null) {
            for(Method method : clazz.getDeclaredMethods()) {

                MethodSignature signature = new MethodSignature(method);

                if(!signatures.contains(signature)) {
                    result.add(method);

                    signatures.add(signature);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return result;
    }

}
