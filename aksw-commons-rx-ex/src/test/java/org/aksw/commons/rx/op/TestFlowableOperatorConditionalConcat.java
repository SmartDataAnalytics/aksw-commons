package org.aksw.commons.rx.op;

import java.util.HashSet;
import java.util.stream.IntStream;

import org.aksw.commons.collector.core.AggBuilder;
import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;

public class TestFlowableOperatorConditionalConcat {

    @Test
    public void test() {
        Flowable<Integer> core = Flowable.fromStream(IntStream.range(0, 20)
                .filter(x -> x % 2 == 0)
                .boxed());

        // Conditionally concat a flow that adds all 'missing' items within the initial range
        core
            .lift(FlowableOperatorConditionalConcat.create(AggBuilder.hashSetSupplier(), items -> Flowable.fromStream(
                    IntStream.range(0, 20).boxed().filter(x -> !items.contains(x)))))
            .blockingForEach(item -> {
                System.out.println("Item " + item);
            });
    }
}
