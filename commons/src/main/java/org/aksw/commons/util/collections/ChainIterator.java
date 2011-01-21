package org.aksw.commons.util.collections;


import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Oct 7, 2010
 * Time: 5:00:39 PM
 */

public class ChainIterator<T>
	extends PrefetchIterator<T>
{
	private Iterator<? extends Iterable<T>>	metaIterator;

	public ChainIterator(Iterator<? extends Iterable<T>> metaIterator)
	{
		this.metaIterator = metaIterator;
	}

	public ChainIterator(Collection<? extends Iterable<T>> metaContainer)
	{
		this.metaIterator = metaContainer.iterator();
	}

	@Override
	protected Iterator<T> prefetch()
	{
		if (!metaIterator.hasNext())
			return null;

		return metaIterator.next().iterator();
	}
}
