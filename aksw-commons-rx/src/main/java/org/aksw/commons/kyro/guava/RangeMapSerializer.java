package org.aksw.commons.kyro.guava;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class RangeMapSerializer
    extends Serializer<RangeMap>
{
    public static <K extends Comparable<K>, V> Map<Range<K>, V> toMap(RangeMap<K, V> rangeMap) {
        return new HashMap<Range<K>, V>(rangeMap.asMapOfRanges());
    }

    public static <K extends Comparable<K>, V> RangeMap<K, V> fromMap(Map<Range<K>, V> map) {
        TreeRangeMap<K, V> result = TreeRangeMap.create();
        map.entrySet().forEach(e -> {
            result.put(e.getKey(), e.getValue());
        });
        return result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void write(Kryo kryo, Output output, RangeMap object) {
        kryo.writeClassAndObject(output, toMap(object));
    }

    @SuppressWarnings("unchecked")
    @Override
    public RangeMap read(Kryo kryo, Input input, Class<RangeMap> type) {
        Map map = (Map)kryo.readClassAndObject(input);
        RangeMap result = fromMap(map);
        return result;
    }
}