package org.aksw.commons.util.reflect;


import org.apache.commons.collections15.map.LRUMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;



class InvocationSignature {
    private Class<?> clazz;
    private String methodName;
    private List<Class<?>> paramTypes;

    InvocationSignature(Class<?> clazz, String methodName, List<Class<?>> paramTypes) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Class<?>> getParamTypes() {
        return paramTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvocationSignature that = (InvocationSignature) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (paramTypes != null ? !paramTypes.equals(that.paramTypes) : that.paramTypes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (paramTypes != null ? paramTypes.hashCode() : 0);
        return result;
    }
}




/*
class InvocationException
{
	Map<Method, int[]
}
*/

public class MultiMethod
{

    // 5000 parameter combinations cached - this might be a bit overkill
    private static LRUMap<InvocationSignature, Method> cache = new LRUMap(5000);

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
	public static <T, X> X invokeStatic(Class<T> clazz, String name, Object ...args)
	{
		Method m = findMethodByArgs(clazz, name, args);

        return (X)ClassUtils.forceInvoke(null, m, args);
	}



	public static <X> X invoke(Object o, String name, Object ...args)
	{
		Method m = findInvocationMethod(o.getClass(), name, args);

		return (X)ClassUtils.forceInvoke(o, m, args);
	}

    public static <T> Map<Method, Integer[]> findMethodCandidates(Collection<Method> candidates, Class<?> ...typeSignature)
    {
        Map<Method, Integer[]> bestMatches = new HashMap<Method, Integer[]>();
        for(Method m : candidates) {

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

        return bestMatches;

    }


    public static <T> Map<Method, Integer[]> findMethodCandidates(Class<T> clazz, String name, Class<?> ...typeSignature)
    {
        Collection<Method> methods = ClassUtils.getAllNonOverriddenMethods(clazz, name);

        Map<Method, Integer[]> result = findMethodCandidates(methods, typeSignature);

        return result;
    }

    /**
     * Errors on lookup (such as no or multiple candidates) ar not cached in will result
     * in new lookups.
     *
     * @param clazz
     * @param name
     * @param typeSignature
     * @param <T>
     * @return
     */
    public static <T> Method findMethodByParamsCached(Class<T> clazz, String name, List<Class<?>> typeSignature) {

        InvocationSignature invocationSignature = new InvocationSignature(clazz, name, typeSignature);

        Method result = cache.get(invocationSignature);
        if(result != null) {
            return result;
        }

        if(cache.containsKey(invocationSignature)) {
            throw new RuntimeException("No method found for given classes");
        } else {
            try {
                result = findMethodByParams(clazz, name, typeSignature.toArray(new Class<?>[0]));
                cache.put(invocationSignature, result);
                return result;
            } catch(RuntimeException e) {
                throw e;
            }
        }
    }

    public static <T> Method findMethodByParams(Class<T> clazz, String name, Class<?> ...typeSignature)
    {
        Map<Method, Integer[]> bestMatches = findMethodCandidates(clazz, name, typeSignature);

		if(bestMatches.size() == 0) {
			throw new NoMethodInvocationException(name, typeSignature);
		} else if(bestMatches.size() > 1) {
			throw new MultipleMethodsInvocationException(name, null, bestMatches.keySet());
		}

		return bestMatches.entrySet().iterator().next().getKey();
    }

    @Deprecated // Use findMethodByArgs instead
    public static <T> Method findInvocationMethod(Class<T> clazz, String name, Object ...args)
    {
        return findMethodByArgs(clazz, name, args);
    }


	public static <T> Method findMethodByArgs(Class<T> clazz, String name, Object ...args)
	{
		List<Class<?>> typeSignature = ClassUtils.getTypeSignatureList(args);

        Method result = findMethodByParamsCached(clazz, name, typeSignature);
        return result;
	}


    /**
     * Find the best matching methods for the given types.
     * Used for casts
     *
     * @param clazz
     * @param returnType
     * @param args
     * @param <T>
     * @return
     */
    public static <T> Method findInvocationMethod(Class<T> clazz, Class<?> returnType, Class<?> ...args)
    {
        String name = "no name";

        Class<?>[] typeSignature = args; //ClassUtils.getTypeSignature(args);

        Map<Method, Integer[]> bestMatches = new HashMap<Method, Integer[]>();
        for(Method m : ClassUtils.getAllNonOverriddenMethods(clazz)) {

            if(m.getParameterTypes().length < args.length && !m.isVarArgs()) {
                continue;
            }

            /*
            if(!m.getName().equals(name)) {
                continue;
            }*/

            Integer[] d = ClassUtils.getDistance(returnType, m.getReturnType(), typeSignature, m.getParameterTypes());;
            if(d == null || Arrays.asList(d).contains(null)) {
                continue;
            }

            //System.out.println("Multimethod Candidate: " + m.getName() + ": " + Arrays.toString(d));

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
