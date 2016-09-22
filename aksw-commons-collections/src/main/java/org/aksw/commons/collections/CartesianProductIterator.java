package org.aksw.commons.collections;


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;


/**
 *
 *
 * FIXME Make use of PrefetchIteratorScala
 *
 * @param <T>
 */
public class CartesianProductIterator<T>
		implements Iterator<List<T>>
{
	/**
	 * The array of iterables which service as suppliers of iterators
	 */
	protected Iterable<? extends T>	iterables[];

	/**
	 * The current array of iterators
	 */
	protected Iterator<? extends T>				iterators[];

	protected T									current[];

	protected T									result[];
	protected List<T>									resultView;

	protected boolean									hasNext	= true;

	protected final int l;
	protected final int lMinusOne;
	protected int copyOffset;

//	public CartesianProductIterator(T[]... collections)
//	{
//
//		this.collections = (Iterable<? extends T>[])new Object[l];
//		System.arraycopy(collections, 0, this.collections, 0, l);
//		//List<List<T>> tmp = new ArrayList<List<T>>(collections.length);
//
////		for (T[] item : collections)
////			tmp.add(Arrays.asList(item));
////
////		this.collections = tmp;
//
//		init();
//	}

	@SuppressWarnings("unchecked")
	public CartesianProductIterator(Collection<? extends Iterable<? extends T>> collections)
	{
		this((Iterable<? extends T>[])collections.toArray(new Iterable[0]));
	}

	@SuppressWarnings("unchecked")
	public CartesianProductIterator(Iterable<? extends T>... iterables)
	{
		this.iterables = iterables;
		this.l = iterables.length;
		this.lMinusOne = l - 1;
		this.iterables = iterables;

		init();
	}


	@SuppressWarnings("unchecked")
	private void init()
	{
		iterators = (Iterator<? extends T>[])new Iterator[l];
		current = (T[])new Object[l];
		result = (T[])new Object[l];

        // If there are no iterators, we're empty
        if(l == 0) {
            this.hasNext = false;
        }

        for(int i = 0; i < l; ++i) {
        	Iterator<? extends T> it = iterables[i].iterator();
        	iterators[i] = it;
			if (it.hasNext()) {
				T value = it.next();
				current[i] = value;
			} else {
				this.hasNext = false;
			}
		}
        copyOffset = 0;

		resultView = Arrays.asList(result); //Collections.unmodifiableList(result);
	}

//	private void init()
//	{
//		iterators = new ArrayList<Iterator<? extends T>>(collections.size());
//		for (Iterable<? extends T> c : collections)
//			iterators.add(c.iterator());
//
//		current = new ArrayList<T>(collections.size());
//		result = new ArrayList<T>(collections.size());
//
//        // If there are no iterators, we're empty
//        if(iterators.isEmpty()) {
//            this.hasNext = false;
//        }
//
//		for (Iterator<? extends T> it : iterators) {
//			if (!it.hasNext()) {
//				this.hasNext = false;
//				return;
//			}
//
//			T value = it.next();
//			current.add(value);
//			result.add(null);
//			// next.add(value);
//		}
//
//		resultView = Collections.unmodifiableList(result);
//	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public List<T> next()
	{
		System.arraycopy(current, copyOffset, result, copyOffset, l - copyOffset);

		// increment iterators
		for (copyOffset = lMinusOne; copyOffset >= 0; --copyOffset) {
			//Iterator<? extends T> it = iterators.get(i);
			Iterator<? extends T> it = iterators[copyOffset];
			// System.out.println("it @ " + copyOffset + " has next? " + it.hasNext());
			// if the iterator overflows => redo the loop and increment the
			// next iterator - otherwise break.
			if (it.hasNext()) {
				T value = it.next();
				current[copyOffset] = value;
				break;
			} else {
				if (copyOffset == 0) {
					hasNext = false;
				}

				it = iterables[copyOffset].iterator();
				iterators[copyOffset] = it;
				current[copyOffset] = it.next();
			}
		}

		return resultView;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException("Operation not supported");
	}
}