package org.aksw.commons.collections.diff;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Claus Stadler
 *
 *         Date: 7/12/11
 *         Time: 10:52 PM
 */
public class HashSetDiff<T>
		extends CollectionDiff<T, Set<T>>
{
	public HashSetDiff()
	{
		super(new HashSet<T>(), new HashSet<T>(), new HashSet<T>());
	}
}