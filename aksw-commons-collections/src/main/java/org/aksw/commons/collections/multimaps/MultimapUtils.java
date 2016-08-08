package org.aksw.commons.collections.multimaps;

import org.aksw.commons.collections.MultiMaps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/25/11
 * Time: 9:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultimapUtils {
	/**
	 * A transitive get in both directions
	 *
	 * @param map
	 */
	public static <T> Set<T> transitiveGetBoth(IBiSetMultimap<T, T> map, Object key)
	{
		Set<T> result = MultiMaps.transitiveGet(map.asMap(), key);
		result.addAll(MultiMaps.transitiveGet(map.getInverse().asMap(), key));

		return result;
	}

    /**
     * Helper function to convert a multimap into a map.
     * Each key may only have at most one corresponding value,
     * otherwise an exception will be thrown.
     *
     * @param mm
     * @return
     */
    public static <K, V> Map<K, V> toMap(Map<K, ? extends Collection<V>> mm) {
        // Convert the multimap to an ordinate map
        Map<K, V> result = new HashMap<K, V>();
        for(Entry<K, ? extends Collection<V>> entry : mm.entrySet()) {
            K k = entry.getKey();
            Collection<V> vs = entry.getValue();
    
            if(!vs.isEmpty()) {
                if(vs.size() > 1) {
                    throw new RuntimeException("Ambigous mapping for " + k + ": " + vs);
                }
    
                V v = vs.iterator().next();
                result.put(k, v);
            }
        }
    
        return result;
    }    
}
