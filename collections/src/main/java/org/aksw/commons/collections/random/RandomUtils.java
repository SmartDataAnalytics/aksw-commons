package org.aksw.commons.collections.random;

import com.google.common.collect.Iterables;

import java.util.*;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:43:50 PM
 * Updated by Konrad Höffner 12.8.2011
 */
public class RandomUtils
{
	/** Returns a random sample of the given universe. If n <= universe.length, then Arrays.copyOf of the universe is returned.
	 * The implementation runs in O(n) (aside from the copy).
	 * @author Konrad Höffner*/
	public static <T> T[] randomSample(T[] universe, int n)
	{
		int m = universe.length;
		if(m<=n) return universe.clone();
		// new T[n] does not work because you cannot create a generic array
		T[] sample =  Arrays.copyOf(universe, n);
	    int rndIndex;
	     Random rnd = new Random();	  
	     for (int i = 0; i < n; i++)
	     {
	         rndIndex = rnd.nextInt(m - i);
	         sample[i] = universe[rndIndex];
	         universe[rndIndex] = universe[m - i -1];
	     }	  	     		
		return sample;
	}

    /**
     * Extracts a random sample of a specified maximum size from the given collection.
     * Warning: Shuffles elements of the source list.
     *
     * @param source
     * @param sampleSize
     * @param <T>
     * @return
     */
    public static <T> void shuffleRandomSample(List<T> source, Collection<T> out, int sampleSize, Random random)
    {
        //if(source.size() < sampleSize)
            //throw new IllegalArgumentException("Cannot create a random sample of size " + sampleSize + " from a collection containing only " + source.size() + " items");

        Collections.shuffle(source, random);
        Iterator<T> it = source.iterator();
        while(out.size() < sampleSize && it.hasNext()) {
            out.add(it.next());
        }
    }

    public static <T> void shuffleRemoveRandomSample(List<T> source, Collection<T> out, int sampleSize, Random random)
    {
        Collections.shuffle(source, random);

        List<T> tmp = source.subList(0, sampleSize);
        out.addAll(tmp);

        tmp.clear();
    }


    public static <T> Set<T> randomSampleSet(Collection<T> source, int sampleSize, Random random)
    {
        List<T> src = new ArrayList(source);
        Set<T> result = new HashSet();

        shuffleRandomSample(src, result, sampleSize, random);

        return result;
    }

    public static <T> Set<T> randomSampleSet(Collection<T> source, int sampleSize)
    {
        return randomSampleSet(source, sampleSize, new Random());
    }


    public static <T> Set<T> shuffleRemoveRandomSampleSet(List<T> source, int sampleSize, Random random)
    {
        Set<T> result = new HashSet<T>();

        shuffleRemoveRandomSample(source, result, sampleSize, random);

        return result;
    }

    public static <T> T randomItem(Collection<T> collection, Random random)
    {
        if(collection.isEmpty())
            throw new IllegalArgumentException("Collection must not be empty");
        
        int index = random.nextInt(collection.size());

        return Iterables.get(collection, index);   
    }

    public static <T> T randomItem(Iterable<T> iterable, Random random)
    {
        if(Iterables.isEmpty(iterable))
            throw new IllegalArgumentException("Iterable must not be empty");

        int index = random.nextInt(Iterables.size(iterable));

        return Iterables.get(iterable, index);
    }
}
