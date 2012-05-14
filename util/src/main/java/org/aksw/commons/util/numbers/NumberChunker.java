package org.aksw.commons.util.numbers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Claus Stadler
 *
 *         Date: 7/13/11
 *         Time: 3:02 PM
 */
public class NumberChunker {
    public static List<Long> chunkValue(long id, long ...chunkSizes)
    {
        List<Long> list = new ArrayList<Long>();
        for(long item : chunkSizes) {
            list.add(item);
        }

        return  chunkValue(id, list);
    }

    /**
     * Given a value, chunks it according to the given bases.
     *
     * This method is used in LinkedGeoData for the
     * directory layout of the changesets
     * e.g. 7531902 becomes (7, 531, 902) with chunks 1000/1000
     *
     *
     * @param id
     * @param chunkSizes
     * @return
     */
    public static List<Long> chunkValue(long id, List<Long> chunkSizes) {
		long denominator = 1;
		for(long chunkSize : chunkSizes)
			denominator *= chunkSize;

		List<Long> result = new ArrayList<Long>();

		long remainder = id;
		for(long chunkSize : chunkSizes) {
			long div = remainder / denominator;
			remainder = remainder % denominator;

			result.add(div);

			denominator = denominator / chunkSize;
		}

		result.add(remainder);

		return result;
	}

}
