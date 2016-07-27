package org.aksw.commons.collections;

import java.util.*;

/**
 * @author Claus Stadler
 * 
 * Date: 7/6/11
 * Time: 10:46 PM
 */
public class StackCartesianProductIterator<T>
    implements Iterator<List<T>>
{
	private List<? extends Iterable<? extends T>>	collections;

    // This list is used as a stack:
    // Iterators from the collections can be pushed and popped
	private List<Iterator<? extends T>>				iterators;

	private List<T>									current;

	private List<T>									result;
	private List<T>									resultView;

	private boolean									hasNext	= true;

	@SafeVarargs
    public StackCartesianProductIterator(T[]... collections)
	{
		List<List<T>> tmp = new ArrayList<List<T>>(collections.length);

		for (T[] item : collections)
			tmp.add(Arrays.asList(item));

		this.collections = tmp;

		init();
	}

	public StackCartesianProductIterator(Iterable<? extends T>... collections)
	{
		this.collections = Arrays.asList(collections);

		init();
	}

	public StackCartesianProductIterator(List<? extends Iterable<? extends T>> collections)
	{
		this.collections = collections;

		init();
	}

	private void init()
	{
		iterators = new ArrayList<Iterator<? extends T>>();
        /*
		for (Iterable<? extends T> c : collections)
			iterators.add(c.iterator());
	    */

		current = new ArrayList<T>();
		result = new ArrayList<T>();

        /*
		for (Iterator<? extends T> it : iterators) {
			if (!it.hasNext()) {
				this.hasNext = false;
				return;
			}

			T value = it.next();
			current.add(value);
			result.add(null);
			// next.add(value);
		}*/

		resultView = Collections.unmodifiableList(result);
	}

    public boolean canPush() {
        return iterators.size() < collections.size();
    }

    public void push() {
        int index = iterators.size();

        if(index >= collections.size()) {
            throw new IndexOutOfBoundsException();
        }

        Iterator<? extends T> it = collections.get(index).iterator();
        if (!it.hasNext()) {
            this.hasNext = false;
            return;
        }

        iterators.add(it);
        current.add(it.next());
        result.add(null);
    }

    public void pop() {
        int index = iterators.size() - 1;
        if(index < 0) {
            throw new EmptyStackException();
        }

        iterators.remove(index);
        result.remove(index);
        current.remove(index);

        // Check if any of the iterators have a next element
        for(int i = 0; i < index; ++i) {
            if(!iterators.get(i).hasNext()) {
                this.hasNext = false;
                return;
            }
        }

        this.hasNext = !iterators.isEmpty();
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