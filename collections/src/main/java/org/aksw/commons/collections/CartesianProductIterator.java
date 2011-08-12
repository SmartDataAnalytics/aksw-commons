package org.aksw.commons.collections;


import java.util.*;


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
	private List<? extends Iterable<? extends T>>	collections;

	private List<Iterator<? extends T>>				iterators;

	private List<T>									current;

	private List<T>									result;
	private List<T>									resultView;

	private boolean									hasNext	= true;

	public CartesianProductIterator(T[]... collections)
	{
		List<List<T>> tmp = new ArrayList<List<T>>(collections.length);

		for (T[] item : collections)
			tmp.add(Arrays.asList(item));

		this.collections = tmp;

		init();
	}

	public CartesianProductIterator(Iterable<? extends T>... collections)
	{
		this.collections = Arrays.asList(collections);

		init();
	}

	public CartesianProductIterator(List<? extends Iterable<? extends T>> collections)
	{
		this.collections = collections;

		init();
	}

	private void init()
	{
		iterators = new ArrayList<Iterator<? extends T>>(collections.size());
		for (Iterable<? extends T> c : collections)
			iterators.add(c.iterator());

		current = new ArrayList<T>(collections.size());
		result = new ArrayList<T>(collections.size());
		for (Iterator<? extends T> it : iterators) {
			if (!it.hasNext()) {
				this.hasNext = false;
				return;
			}

			T value = it.next();
			current.add(value);
			result.add(null);
			// next.add(value);
		}

		resultView = Collections.unmodifiableList(result);
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public List<T> next()
	{
		if (!hasNext)
			return null;

		for (int i = 0; i < current.size(); ++i)
			result.set(i, current.get(i));
		// swap current and next
		// List<T> tmp = current;
		// current = next;
		// next = tmp;

		// List<T> viewTmp = currentView;
		// currentView = nextView;
		// nextView = tmp;

		// increment iterators
		for (int i = iterators.size() - 1; i >= 0; --i) {
			Iterator<? extends T> it = iterators.get(i);

			// if the iterator overflows => redo the loop and increment the
			// next iterator - otherwise break.
			if (!it.hasNext()) {
				if (i == 0)
					hasNext = false;

				it = collections.get(i).iterator();
				iterators.set(i, it);
				current.set(i, it.next());
			} else {
				current.set(i, it.next());
				break;
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