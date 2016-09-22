package org.aksw.commons.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * @author Claus Stadler
 *
 * Date: 7/6/11
 * Time: 11:01 PM
 */
public class CartesianIteratorTest {

	public static void main(String[] args) {
		Stopwatch sw = Stopwatch.createStarted();
		for(int y = 0; y < 10; ++y) {
			new CartesianIteratorTest().testCartesianProduct();

			//System.out.println("done[" + y + "]: " + i);
		}
		System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testCartesianProduct() {
		List<Integer> ints = IntStream.range(0, 10).mapToObj(x -> x).collect(Collectors.toList());

		List<List<Integer>> arr = Arrays.asList(ints, ints, ints);
		Iterator<List<Integer>> itA = Lists.cartesianProduct(arr).iterator();
		Iterator<List<Integer>> itB = CartesianProduct.create(arr).iterator();

		while(itA.hasNext() && itB.hasNext()) {
			List<Integer> a = itA.next();
			List<Integer> b = itB.next();

			if(!a.equals(b)) {
				Assert.assertEquals(a, b);
			}
		}

		boolean unfinished = itA.hasNext() || itB.hasNext();
		Assert.assertFalse(unfinished);
	}


    // The implementation of the StackCartesianProductIterator does not terminate
    //@Test
    public void testStack() {
        List<String> a = Arrays.asList(new String[] {"a", "b"});
        List<String> b = Arrays.asList(new String[] {"1", "2", "3"});
        List<String> c = Arrays.asList(new String[] {"this", "is", "a" , "test"});


        StackCartesianProductIterator<String> it = new StackCartesianProductIterator<String>(a, b,c);

        // We need to push once, so we can start iterating
        it.push();

        while (it.hasNext()) {


            while (it.canPush()) {
                it.push();

                List<String> current = it.next();

                if(!isAccepted(current)) {
                    it.pop();
                    it.next();
                }


                System.out.println(current);

            }
        }

    }

    public static boolean isAccepted(List<String> list) {
        return !list.get(1).equals(2);
    }
}
