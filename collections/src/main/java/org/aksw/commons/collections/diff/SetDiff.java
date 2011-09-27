package org.aksw.commons.collections.diff;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/12/11
 *         Time: 10:52 PM
 */
public class SetDiff<T>
	extends CollectionDiff<T, Set<T>>
{
	public SetDiff(Set<T> newItems, Set<T> oldItems)
	{
		super(
				Sets.difference(newItems, oldItems),
				Sets.difference(oldItems, newItems),
				Sets.intersection(newItems, oldItems)
		);
	}

	public SetDiff(Set<T> added, Set<T> removed, Set<T> retained)
	{
		super(added, removed, retained);
	}

}
