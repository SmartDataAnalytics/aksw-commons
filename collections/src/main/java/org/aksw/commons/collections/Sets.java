package org.aksw.commons.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Contains static result-returning methods for the basic set operations union, intersection, difference and complement.
 * This was created because the author hates math with void methods. Using static import de.konradhoeffner.commons.Sets
 * one can write "Set x = intersection(union(a,b),c);". In standard java-lingo this would be:</br>
 * <pre><code>Set x = new HashSet(a);
 *x.addAll(b);
 *x.retainAll(c);</code><pre>*/
public class Sets
{
	/** Returns the set union of the two collections s and t. Changing the result does not change s or t and vice versa.*/
	static <T> Set<T> union(Collection<T> s,Collection<T> t)
	{
		Set<T> union = new HashSet<T>(s);
		union.addAll(t);
		return union;
	}
	
	static <T> Set<T> intersection(Collection<T> s,Collection<T> t)
	{
		Set<T> intersection = new HashSet<T>(s);
		intersection.retainAll(t);
		return intersection;
	}
	
	static <T> Set<T> difference(Collection<T> s,Collection<T> t)
	{
		Set<T> difference = new HashSet<T>(s);
		difference.removeAll(t);
		return difference;
	}
	
	static <T> Set<T> complement(Collection<T> s,Collection<T> universe)
	{
		return difference(universe,s);
	}
}