package org.aksw.commons.rx.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;

public class FlowableOperatorSequentialGroupByTests {


    @Test
    public void test1() {
        Iterable<List<Integer>> lists = Flowable.fromIterable(Arrays.asList(2, 4, 1, 3, 5))
            .lift(FlowableOperatorSequentialGroupBy.create(SequentialGroupBySpec.create(item -> item % 2, () -> (List<Integer>)new ArrayList<Integer>(), Collection::add)))
            .map(Entry::getValue)
            .blockingIterable();

        for (List<Integer> list : lists) {
            System.out.println("test1: " + list);
        }

    }

    @Test
    public void test2() {
        List<Integer> list = Flowable.fromIterable(Arrays.asList(2, 4, 1, 3, 5))
            .lift(FlowableOperatorSequentialGroupBy.create(SequentialGroupBySpec.create(item -> item % 2, () -> (List<Integer>)new ArrayList<Integer>(), Collection::add)))
            .map(Entry::getValue)
            .first(Collections.emptyList())
            .blockingGet();

        System.out.println("test2: " + list);
    }

}
