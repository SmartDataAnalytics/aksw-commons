package org.aksw.commons.collector.core;

import java.util.Set;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableBiFunction;
import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializablePredicate;

// Experiment aimed at trying out whether a chaining interface is somewhat more
// useable; but so far it isn't - see the example() function below
@FunctionalInterface
public interface AggBuilderFn<
        XI, XO, XACC extends Accumulator<XI, XO>, XAGG extends ParallelAggregator<XI, XO, XACC>,
        YI, YO, YACC extends Accumulator<YI, YO>, YAGG extends ParallelAggregator<YI, YO, YACC>>
    extends SerializableFunction<XAGG, YAGG>
{

    default
    <ZI, ZO, ZACC extends Accumulator<ZI, ZO>, ZAGG extends ParallelAggregator<ZI, ZO, ZACC>>
    AggBuilderFn<
        XI, XO, XACC, XAGG,
        ZI, ZO, ZACC, ZAGG>
    andThen(AggBuilderFn<YI, YO, YACC, YAGG, ZI, ZO, ZACC, ZAGG> next) {
        return in -> {
            YAGG o = this.apply(in);
            ZAGG r = next.apply(o);
            return r;
        };
    }

    static <XI, XO, XACC extends Accumulator<XI, XO>, XAGG extends ParallelAggregator<XI, XO, XACC>,
            YI, YO, YACC extends Accumulator<YI, YO>, YAGG extends ParallelAggregator<YI, YO, YACC>>
    AggBuilderFn<XI, XO, XACC, XAGG, YI, YO, YACC, YAGG> of(AggBuilderFn<XI, XO, XACC, XAGG, YI, YO, YACC, YAGG> fn) {
        return fn;
    }



    public static <I, O,
                    SUBACC extends Accumulator<I, O>,
                    SUBAGG extends ParallelAggregator<I, O, SUBACC>>
    SerializableFunction<SUBAGG, AggInputFilter<I, O, SUBACC, SUBAGG>>
    inputFilter(SerializablePredicate<? super I> inputFilter) {
        return subAgg -> AggBuilder.inputFilter(inputFilter, subAgg);
    }

    public static <I, K, J, O,
        SUBACC extends Accumulator<J, O>,
        SUBAGG extends ParallelAggregator<J, O, SUBACC>>
    SerializableFunction<SUBAGG, AggInputSplit<I, K, J, O, SUBACC, SUBAGG>>
    inputSplit(
        SerializableFunction<? super I, ? extends Set<? extends K>> keyMapper,
        SerializableBiFunction<? super I, ? super K, ? extends J> valueMapper) {

        return subAgg -> AggBuilder.inputSplit(keyMapper, valueMapper, subAgg);
    }


    static void example() {
        // Issue: We'd need to know the values we are aggregating when starting the chain
        /*
        AggBuilderFn
            .of(AggBuilderFn.inputFilter((String x) -> x != null)::apply)
            .andThen(AggBuilderFn.inputFilter((String x) -> x != null))
            //.andThen(AggBuilderFn.inputSplit((String in) -> Collections.<String>singleton(in), (Set<String> in, String k) -> k))
            .apply(AggBuilder.<String>hashSetSupplier());
            //.andThen(AggBuilder.hashSetSupplier());
         */
    }

}
