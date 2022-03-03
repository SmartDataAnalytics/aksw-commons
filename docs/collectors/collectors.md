# Aggregator / Collector Builder

## Essentials

The [AggBuilder](https://github.com/SmartDataAnalytics/aksw-commons/blob/develop/aksw-commons-collectors/src/main/java/org/aksw/commons/collector/core/AggBuilder.java) is the entry point for composing parallelizable aggregators.

The framework is designed to be both usable with Java8 streams as well as with Apache Spark.
The latter is the reason why all builder functions take *serializable* lambdas as arguments, so that their state can be transferred across a cluster.

The main interface is `ParallelAggregator`:

```java
public interface ParallelAggregator<I, O, ACC extends Accumulator<I, O>>
	 extends Aggregator<I, O>
{
    ACC createAccumulator();

    // Combine two accumulators. May mutate the arguments and return either of them.
    ACC combine(ACC a, ACC b);

    default Collector<I, ?, O> asCollector() {
        return new CollectorFromParallelAggregator<>(this);
    }

    default ACC combineRaw(Object x, Object y) { ... }
}
```

An `Accumulator` can receive items as input which are turned into a value: */

```java
public interface Accumulator<I, O> {
    void accumulate(I input);
    O getValue();
}
```

## The AggBuilder

* inputFilter: A predicate on the input determines whether it is forwarder to a sub aggregator.

* inputSplit: Create the *same* accumulator type for each split of the input

* inputBroadcast: An aggregator that broadcasts its input to multiple sub-aggregators that accept the same input

* inputBroadcastMap: Pass every input to given a map of aggregators

* inputTransform: Transform the input before passing it on to a sub aggregator

* outputTransform: Decorate the getValue() method of the accumulator with a transformation

* reduce: Given a supplier of 'zero' elements and a binary operator. Useful for min/max computation.

* counting: Count the number of items passed to `accumulator`.






