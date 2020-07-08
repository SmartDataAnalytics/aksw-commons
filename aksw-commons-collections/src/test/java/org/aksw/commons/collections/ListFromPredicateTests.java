package org.aksw.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.aksw.commons.accessors.ListFromPredicate;
import org.junit.Test;

public class ListFromPredicateTests {

    @Test
    public void testFilteredList() {
        List<Integer> core = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        List<Integer> list = new ListFromPredicate<>(core, item -> item % 2 == 0);

//        list = core;

        list.listIterator(1).add(8);
        System.out.println(list.listIterator(2).next());
        ListIterator<Integer> it = list.listIterator(4);

        System.out.println(it.hasNext());
        System.out.println(it.previous());

        it.remove();
//        it.next();
//        it.remove();


        list.set(2, 666);
        list.add(888);

        ListIterator<Integer> it2 = list.listIterator(list.size());
        it2.previous();
        it2.set(444);


        System.out.println("List (size=" + list.size() + "): " + list);
    }
}
