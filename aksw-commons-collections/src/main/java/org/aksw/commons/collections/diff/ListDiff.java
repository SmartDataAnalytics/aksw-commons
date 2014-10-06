package org.aksw.commons.collections.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/5/12
 *         Time: 12:44 AM
 */
public class ListDiff<T>
		extends CollectionDiff<T, List<T>>
{
	public ListDiff()
	{
		super(new ArrayList<T>(), new ArrayList<T>(), new ArrayList<T>());
	}
}
