package org.aksw.commons.collections;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/22/11
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapUtils {
	/**
	 * Compatible means that merging the two maps would not result in the same
	 * key being mapped to distinct values.
	 *
	 * Or put differently:
	 * The union of the two maps retains a functional mapping.
	 *
	 * @param <K>
	 * @param <V>
	 * @param a
	 * @param b
	 * @return
	 */
	public static <K, V> boolean isCompatible(Map<K, V> a, Map<K, V> b) {
		return isPartiallyCompatible(a, b) && isPartiallyCompatible(b, a);
	}

	public static <K, V> boolean isPartiallyCompatible(Map<K, V> a, Map<K, V> b) {
		for(Map.Entry<K, V> entry : a.entrySet()) {
			K key = entry.getKey();
			V vA = entry.getValue();
			V vB = b.get(key);

			if(vA == null) {
				if(vB != null)
					return false;
			} else {
				if(!vA.equals(vB) && b.containsKey(key)) // Note: if the values differ, it might by that vB equals null since it doesn't exist
					return false;
			}
		}

		return true;
	}

	public static <K, V> V getOrElse(Map<? extends K, ? extends V> map, K key, V elze)
	{
		if(map.containsKey(key)) {
			return map.get(key);
		}

		return elze;
	}

	public static <K, V> Map<K, V> createChainMap(Map<K, ?> a, Map<?, V> b) {
		Map<K, V> result = new HashMap<K, V>();

		for(Map.Entry<K, ?> entry : a.entrySet()) {
			if(b.containsKey(entry.getKey())) {
				result.put(entry.getKey(), b.get(entry.getKey()));
			}
		}

		return result;
	}
}
