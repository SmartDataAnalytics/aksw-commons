package org.aksw.commons.util.collections;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.iterators.TransformIterator;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Oct 8, 2010
 * Time: 5:34:10 PM
 */
public class TransformCollection<I, O>
	extends AbstractCollection<O>
{
	private Collection<I> src;
	private Transformer<I, O> transformer;

	public TransformCollection(Collection<I> src, Transformer<I, O> transformer)
	{
		if(src == null)
			src = Collections.emptySet();

		this.src = src;
		this.transformer = transformer;
	}

	public Iterator<O> iterator()
	{
		return new TransformIterator<I, O>(src.iterator(), transformer);
	}

    @Override
    public int size() {
        return src.size();
    }

    public static <I, O> Collection<O> transformedView(Collection<I> src, Transformer<I, O> transformer)
	{
		return new TransformCollection<I, O>(src, transformer);
	}
}
