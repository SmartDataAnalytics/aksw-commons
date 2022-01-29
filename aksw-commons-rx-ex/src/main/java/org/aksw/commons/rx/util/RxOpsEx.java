package org.aksw.commons.rx.util;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.Aggregator;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

public class RxOpsEx {
	
	public static <I, O> FlowableTransformer<I, O> wrap(Aggregator<I, O> aggregator) {
		return upstream -> {
			Accumulator<I, O> accumulator = aggregator.createAccumulator();
			upstream.forEach(accumulator::accumulate);
			return Flowable.just(accumulator.getValue());
		};
	}

}
