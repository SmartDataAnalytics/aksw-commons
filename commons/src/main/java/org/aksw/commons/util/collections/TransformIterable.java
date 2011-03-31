package org.aksw.commons.util.collections;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.iterators.TransformIterator;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Oct 8, 2010
 * Time: 5:32:34 PM
 */
public class TransformIterable<I, O>
	implements Iterable<O>
{
	private Iterable<I> src;
	private Transformer<I, O> transformer;

	public TransformIterable(Iterable<I> src, Transformer<I, O> transformer)
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

	public static <I, O> Iterable<O> transformedView(Iterable<I> src, Transformer<I, O> transformer)
	{
		return new TransformIterable<I, O>(src, transformer);
	}
}
