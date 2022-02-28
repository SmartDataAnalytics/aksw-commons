package org.aksw.commons.store.object.key.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.function.Consumer;

import org.aksw.commons.kryo.guava.EntrySerializer;
import org.aksw.commons.kryo.guava.RangeMapSerializer;
import org.aksw.commons.kryo.guava.RangeSetSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

public class KryoUtils {
    public static KryoPool createKyroPool(Consumer<Kryo> customRegistrator) {
        KryoFactory factory = new KryoFactory() {
            public Kryo create() {
                Kryo kryo = new Kryo();

                Serializer<?> javaSerializer = new JavaSerializer();
                Serializer<?> rangeSetSerializer = new RangeSetSerializer();
                Serializer<?> rangeMapSerializer = new RangeMapSerializer();
                Serializer<?> entrySerializer = new EntrySerializer();

                kryo.register(TreeRangeSet.class, rangeSetSerializer);
                kryo.register(TreeRangeMap.class, rangeMapSerializer);
                kryo.register(Range.class, javaSerializer);
                kryo.register(SimpleEntry.class, entrySerializer);

                if (customRegistrator != null) {
                    customRegistrator.accept(kryo);
                }
                return kryo;
            }
        };
        // Build pool with SoftReferences enabled (optional)
        KryoPool result = new KryoPool.Builder(factory).softReferences().build();
        return result;
    }
}
