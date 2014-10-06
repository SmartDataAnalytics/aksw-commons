package org.aksw.commons.collections.multimaps;

import org.aksw.commons.collections.MultiMaps;

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
}
