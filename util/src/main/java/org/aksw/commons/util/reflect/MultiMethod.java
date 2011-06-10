package org.aksw.commons.util.reflect;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;






/*
class InvocationException
{
	Map<Method, int[]
}
*/

public class MultiMethod
{
	/**
	 * Invoke the method of an object, that matches the name and arguments best.
	 *
	 * TODO Add some caching mechanism
	 *
	 * @param <T>
	 * @param clazz
	 * @param name
	 * @param args
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static <T> Object invokeStatic(Class<T> clazz, String name, Object ...args)
	{
		Method m = findInvocationMethod(clazz, name, args);

        return ClassUtils.forceInvoke(null, m, args);
	}



	public static Object invoke(Object o, String name, Object ...args)
	{
		Method m = findInvocationMethod(o.getClass(), name, args);

		return ClassUtils.forceInvoke(o, m, args);
	}

	public static <T> Method findInvocationMethod(Class<T> clazz, String name, Object ...args)
	{
		Class<?>[] typeSignature = ClassUtils.getTypeSignature(args);


		Map<Method, Integer[]> bestMatches = new HashMap<Method, Integer[]>();
		for(Method m : clazz.getMethods()) {
			if(!m.getName().equals(name)) {
				continue;
			}


			Integer[] d = ClassUtils.getDistance(typeSignature, m.getParameterTypes());
			if(d == null || Arrays.asList(d).contains(null)) {
				continue;
			}


			// All matches that are worse than current candidate are removed
			// The candidate is only added, if it is not worse than any of the
			// other candidates
			boolean canBeAdded = true;
			for(Iterator<Entry<Method, Integer[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
				Entry<Method, Integer[]> entry = it.next();

				int rel = ClassUtils.getRelation(d, entry.getValue());

				if(rel == -1) {
					it.remove();
				} else if(rel > 0) {
					canBeAdded = false;
				}
			}

			if(canBeAdded) {
				bestMatches.put(m, d);
			}
		}

		if(bestMatches.size() == 0) {
			throw new NoMethodInvocationException(name, args);
		} else if(bestMatches.size() > 1) {
			throw new MultipleMethodsInvocationException(name, args, bestMatches.keySet());
		}

		return bestMatches.entrySet().iterator().next().getKey();
	}
}
