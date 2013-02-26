package org.aksw.commons.collections.diff;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/12/11
 *         Time: 10:53 PM
 */
public class TreeSetDiff<T>
		extends CollectionDiff<T, Set<T>>
{
	/*
	 * public SetDiff(Comparator<T> comparator) { }
	 */
	public TreeSetDiff()
	{
		super(new TreeSet<T>(), new TreeSet<T>(), new TreeSet<T>());
	}

	public TreeSetDiff(Comparator<T> comparator)
	{
		super(new TreeSet<T>(comparator), new TreeSet<T>(comparator),
				new TreeSet<T>(comparator));
	}
}
