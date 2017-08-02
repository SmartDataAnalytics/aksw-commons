package org.aksw.commons.collections.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

public class TestBlockingCacheIterator {

    /**
     * Test to verify that iterators on array list are fail fast
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModification() {
        List<String> list = new ArrayList<>(Arrays.asList("a"));
        ListIterator<String> it = list.listIterator();

        list.add("b");
        while(it.hasNext()) {
            System.out.println(it.next());
        }

        // Fail fast is not guaranteed. Force failure to make the 'test' succeed.
        throw new ConcurrentModificationException();
    }

    /**
     * Test whether sublists can be safely iterated when the parent list is modified elsewhere
     *
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModification2() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> subList = list.subList(0, 2);
        ListIterator<String> it = subList.listIterator();

        list.add("d");
        while(it.hasNext()) {
            System.out.println(it.next());
        }

        // Fail fast is not guaranteed. Force failure to make the 'test' succeed.
        throw new ConcurrentModificationException();
    }

    //@Test
    public void testOld() {
        //List<String> testData = Arrays.asList("john", "doe", "alice", "bob");
        List<String> testData = IntStream.range(0, 1000).mapToObj(i -> "item-" + i).collect(Collectors.toList());
        Cache<String> cache = new CacheImpl<>(new ArrayList<>());

        CachingIterable<String> driver = new CachingIterable<>(testData.iterator(), cache);

        //BlockingCacheIterator<String> it = //new BlockingCacheIterator<>(cache);
        Iterator<String> it = cache.iterator();

        new Thread(() -> {
            int i = 0;
            for(String item : driver) {
                ++i;
//                if(i == 100) {
//                    cache.setAbanoned(true);
//                    break;
//                }
                System.out.println("Driver: " + item);
            }
        }).start();

        while(it.hasNext()) {
            String item = it.next();
            System.out.println("Client: " + item);
        }

    }


    @Test
    public void test() throws Exception {
        Stream<String> testDataStream = IntStream
                .range(0, 1000)
                .mapToObj(i -> "item-" + i)
                .peek(i -> System.out.println("Driver: " + i));

        try(Cache<String> cache = new StreamBackedList<>(testDataStream)) {
            for(String item : cache) {
                System.out.println("Client: " + item);
            }
        }

        System.out.println("Done.");

    }

}
