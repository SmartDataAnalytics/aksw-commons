package org.aksw.commons.util.reflect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 6/3/11
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassUtils {
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

    public static int[] getDistance(Class<?>[] a, Class<?>[] b)
    {
        int n = Math.min(a.length, b.length);

        int[] result = new int[n];
        for(int i = 0; i < n; ++i) {
            result[i] = getDistance(a[i], b[i]);
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
    public static Integer getRelation(int[] a, int[] b)
    {
        boolean hasGreater = false;
        boolean hasLess = false;

        for(int i = 0; i < a.length; ++i) {
            int d = b[i] - a[i];

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
}
