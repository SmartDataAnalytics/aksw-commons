package org.aksw.commons.collections;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/10/11
 *         Time: 12:03 PM
 */

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 * Derived from
 * StackCartesianProductIterator<String> it = new StackCartesianProductIterator<String>(a, b,c);
 */
public class DescenderIterator<T> implements Iterator<List<T>> {
	// This list is used as a stack:
	// Every current element may be descended into
	//private List<Collection<? extends T>> collections = new ArrayList<Collection<? extends T>>();
	private List<Iterator<? extends T>> iterators = new ArrayList<Iterator<? extends T>>();
	private List<T> current = new ArrayList<T>();


	//private List<T> result;
	private List<T> resultView;

	private Descender<T> descender;

    //private Iterable<T> currentChildren = null;
    private Iterator<T> childIterator = null;
    private boolean canDescend = false;

    //private boolean hasNext = true;
    private boolean nextCalled = true;
    private boolean finished = false;

	public DescenderIterator(T base, Descender<T> descender) {
		this.descender = descender;

        iterators.add(Collections.singleton(base).iterator());
        current.add(null);

        resultView = Collections.unmodifiableList(current);
	}



	public static <T> List<Integer> getIndexesOfEmptySubIterables(List<? extends Iterable<? extends T>> iterables) {
		List<Integer> result = new ArrayList<Integer>();

		for(int i = 0; i < iterables.size(); ++i) {
			Iterable<? extends T> iterable = iterables.get(i);

			if(Iterables.isEmpty(iterable)) {
				result.add(i);
			}
		}

		return result;
	}


    public void loadChildren() {
        if(current.isEmpty()) {
            canDescend = false;
            return;
        }

        if(childIterator != null) {
            return;
        }

        T item = current.get(current.size() - 1);

        childIterator = descender.getDescendCollection(item).iterator();
        canDescend = childIterator.hasNext();
    }

	public boolean canDescend() {
        loadChildren();
        return canDescend;
	}


    /**
     * Set the iterator to the children of the current node.
     *
     */
	public void descend() {
        loadChildren();

        iterators.add(childIterator);
        current.add(null);
        nextCalled = true;

        childIterator = null;
	}


	@Override
	public boolean hasNext() {
        prepareNext();
		return !finished;
	}


    private void prepareNext() {
		if (finished || !nextCalled)
			return;

        nextCalled = false;

        //adjustResultSize();
        childIterator = null;

		// increment iterators
		for (int i = iterators.size() - 1; i >= 0; --i) {
			Iterator<? extends T> it = iterators.get(i);

			// if the iterator overflows => redo the loop and increment the
			// next iterator - otherwise break.
			if (!it.hasNext()) {
				if(i == 0) {
                    finished = true;
					break;
				}

				iterators.remove(i);
				current.remove(i);

			} else {
				T item = it.next();
				current.set(i, item);

				break;
			}
		}
    }

	@Override
	public List<T> next() {
        prepareNext();
        nextCalled = true;

        // TODO Potentially close the child iterator
        childIterator = null;

		return resultView;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}
}