package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.collector.domain.Accumulator;

//public class AccMap<K, V, M extends Map<K, V>>
//    implements Accumulator<Entry<K, V>, M>, Serializable
//{
//    private static final long serialVersionUID = 0;
//
//    protected M value;
//
//    public AccMap(M value) {
//        super();
//        this.value = value;
//    }
//
//    @Override
//    public void accumulate(Entry<K, V> item) {
//        value.put(item.getKey(), item.getValue());
//    }
//
//    @Override
//    public M getValue() {
//        return value;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((value == null) ? 0 : value.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        AccMap<?, ?, ?> other = (AccMap<?, ?, ?>) obj;
//        if (value == null) {
//            if (other.value != null)
//                return false;
//        } else if (!value.equals(other.value))
//            return false;
//        return true;
//    }
//}