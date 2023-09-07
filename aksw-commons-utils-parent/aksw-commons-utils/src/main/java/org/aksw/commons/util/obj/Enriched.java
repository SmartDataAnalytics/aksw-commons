package org.aksw.commons.util.obj;

import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 * A class for annotating an 'item' with metadata of arbitrary types.
 */
public class Enriched<T> {
    protected T item;
    protected ClassToInstanceMap<Object> classToInstanceMap;

    public Enriched(T item) {
        this(item, MutableClassToInstanceMap.create());
    }

    public Enriched(T item, ClassToInstanceMap<Object> classToInstanceMap) {
        super();
        this.item = item;
        this.classToInstanceMap = classToInstanceMap;
    }

    public T getItem() {
        return item;
    }

    public ClassToInstanceMap<Object> getClassToInstanceMap() {
        return classToInstanceMap;
    }

    public <X> X getInstance(Class<X> type) {
        return classToInstanceMap.getInstance(type);
    }

    public <X> X getInstanceOrDefault(Class<X> type, X defaultValue) {
        X result = classToInstanceMap.getInstance(type);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    public <X> X putInstance(X value) {
        Class<?> type = value.getClass();
        classToInstanceMap.put(type, value);
        return value;
    }

    public <X> X putInstance(Class<X> type, X value) {
        return classToInstanceMap.putInstance(type, value);
    }

    public <X> X getOrCreateInstance(Class<X> type, Supplier<X> ctor) {
        X result = classToInstanceMap.getInstance(type);
        if (result == null) {
            result = ctor.get();
            classToInstanceMap.putInstance(type, result);
        }
        return result;
    }

    public static <T> Enriched<T> of(T item) {
        return new Enriched<>(item);
    }

    public static <T> Enriched<T> of(T item, Object value) {
        Enriched<T> result = of(item);
        if (value != null) {
            result.getClassToInstanceMap().put(value.getClass(), value);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, classToInstanceMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Enriched<?> other = (Enriched<?>) obj;
        return Objects.equals(item, other.item) && Objects.equals(classToInstanceMap, other.classToInstanceMap);
    }

    @Override
    public String toString() {
        return "EnrichedItem [item=" + item + ", classToInstanceMap=" + classToInstanceMap + "]";
    }



//    public static <T, X> EnrichedItem<T> of(T item, ClassObject value) {
//        EnrichedItem<T> result = of(item);
//        if (value != null) {
//            result.getClassToInstanceMap().put(value.getClass(), value);
//        }
//        return result;
//    }
}
