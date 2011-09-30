package org.aksw.commons.collections;

import java.util.Comparator;
import java.util.Map;

/**Compares elements based on the order of their values in a given map.  
 * Based on <a href="http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java"/>
 * Stack Overflow: How to sort a Map<Key, Value> on the values in Java?<a/> .
 * @author Konrad Höffner */
public class ValueComparator<S,T extends Comparable<T>> implements Comparator<S>
{
	Map<S,T> map;
	public 			ValueComparator(Map<S, T> map)	{setMap(map);}
	public void	setMap(Map<S, T> map)			{this.map = map;}
	
	@Override
	public int compare(S s, S t) {return map.get(s). compareTo(map.get(t));}
}