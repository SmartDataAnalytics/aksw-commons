package org.aksw.commons.collections;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Claus Stadler
 *
 * Date: 7/6/11
 * Time: 11:01 PM
 */
public class CartesianIteratorTest {
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
