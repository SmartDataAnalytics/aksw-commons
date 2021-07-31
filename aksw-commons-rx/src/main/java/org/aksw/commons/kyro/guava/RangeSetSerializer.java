package org.aksw.commons.kyro.guava;

import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class RangeSetSerializer
    extends Serializer<RangeSet>
{
    public static <T extends Comparable<T>> Set<Range<T>> toSet(RangeSet<T> rangeSet) {
        return new HashSet<Range<T>>(rangeSet.asRanges());
    }

    public static <T extends Comparable<T>> RangeSet<T> fromSet(Set<Range<T>> set) {
        TreeRangeSet<T> result = TreeRangeSet.create();
        result.addAll(set);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Kryo kryo, Output output, RangeSet object) {
        kryo.writeClassAndObject(output, toSet(object));
    }

    @SuppressWarnings("unchecked")
    @Override
    public RangeSet read(Kryo kryo, Input input, Class<RangeSet> type) {
        Set set = (Set)kryo.readClassAndObject(input);
        RangeSet result = fromSet(set);
        return result;
    }
}