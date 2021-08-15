package org.aksw.commons.kyro.guava;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class EntrySerializer
    extends Serializer<Entry>
{
    protected Object[] toArray(Entry entry) {
        return new Object[] { entry.getKey(), entry.getValue() };
    }

    protected Entry fromArray(Object[] arr) {
        return new SimpleEntry(arr[0], arr[1]);
    }

    @Override
    public void write(Kryo kryo, Output output, Entry object) {
        kryo.writeClassAndObject(output, toArray(object));

    }

    @Override
    public Entry read(Kryo kryo, Input input, Class<Entry> type) {
        Object[] arr = (Object[])kryo.readClassAndObject(input);
        Entry result = fromArray(arr);
        return result;
    }

}
