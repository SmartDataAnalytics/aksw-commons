package org.aksw.commons.collections;

import com.google.common.collect.Iterables;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/24/11
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class CartesianProduct<T>
    extends AbstractCollection<List<T>>
{
    protected List<? extends Iterable<? extends T>>	collections;
    protected boolean inPlace;

	public CartesianProduct(List<? extends Iterable<? extends T>> collections)
	{
		this(false, collections);
	}

	public CartesianProduct(boolean inPlace, List<? extends Iterable<? extends T>> collections)
	{
		this.inPlace = inPlace;
		this.collections = collections;
	}

    public static <T> CartesianProduct<T> create(List<? extends Iterable<? extends T>> collections)
    {
        return new CartesianProduct<T>(false, collections);
    }

    public static <T> CartesianProduct<T> create(Iterable<? extends Iterable<? extends T>> iterables)
    {
        List<Iterable<? extends T>> tmp = new ArrayList<Iterable<? extends T>>();
        for(Iterable<? extends T> item : iterables) {
            tmp.add(item);
        }

        return new CartesianProduct<T>(false, tmp);
    }


    public static <T> CartesianProduct<T> create(T[]... collections)
    {
        List<List<T>> tmp = new ArrayList<List<T>>(collections.length);

        for (T[] item : collections)
            tmp.add(Arrays.asList(item));

        return  new CartesianProduct<T>(false, tmp);
    }

    public static <T> CartesianProduct<T> create(Iterable<? extends T>... collections)
    {
        return new CartesianProduct<T>(false, Arrays.asList(collections));
    }


    //public static <T> CartesianProduct<T> crea

    @Override
    public Iterator<List<T>> iterator() {
        return new CartesianProductIterator<T>(inPlace, collections);
    }

    @Override
    public int size() {
        int size = collections.isEmpty() ? 0 : 1;

        for(Iterable<? extends T> item : collections) {
            size *= Iterables.size(item);
        }

        return size;
    }
}
