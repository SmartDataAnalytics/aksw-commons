package org.aksw.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Test;

public class FilteredListTest {

    @Test
    public void testFilteredList() {
        List<Integer> core = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        List<Integer> list = new FilteringList<>(core, item -> item % 2 == 0);

//        list = core;

        list.listIterator(1).add(8);
        Assert.assertEquals(Arrays.asList(0, 8, 2, 4), list);
        Assert.assertEquals(Arrays.asList(0, 8, 1, 2, 3, 4, 5), core);

        // Starting at index 2 should yield the number 2
        Assert.assertEquals(2l, list.listIterator(2).next().longValue());


        // Position after the last index and try to remove the last item
        ListIterator<Integer> it = list.listIterator(4);
        Assert.assertFalse(it.hasNext());
        Assert.assertEquals(4l, it.previous().longValue());

        it.remove();

        Assert.assertEquals(Arrays.asList(0, 8, 2), list);
        Assert.assertEquals(Arrays.asList(0, 8, 1, 2, 3, 5), core);

        // Set the value at a certain index
        list.set(2, 666);
        Assert.assertEquals(Arrays.asList(0, 8, 1, 666, 3, 5), core);
        Assert.assertEquals(666l, list.get(2).intValue());
        Assert.assertEquals(666l, core.get(3).intValue());


        // Append a new element
        list.add(888);
        Assert.assertEquals(Arrays.asList(0, 8, 1, 666, 888, 3, 5), core);
        Assert.assertEquals(Arrays.asList(0, 8, 666, 888), list);

        // Update the last item
        ListIterator<Integer> it2 = list.listIterator(list.size());
        it2.previous();
        it2.set(444);
        Assert.assertEquals(Arrays.asList(0, 8, 1, 666, 444, 3, 5), core);
        Assert.assertEquals(Arrays.asList(0, 8, 666, 444), list);


        System.out.println("List (size=" + list.size() + "): " + list);
    }
}
