package org.aksw.commons.beans.datatype;

import java.awt.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DataTypes {

    public static CollectionType newCollectionType(Class<?> collectionType, Class<?> itemType) {
        CollectionType result;
        if (List.class.isAssignableFrom(collectionType)) {
            result = new ListTypeImpl(of(collectionType), of(itemType));
        } else if (Set.class.isAssignableFrom(collectionType)) {
            result = new SetTypeImpl(of(collectionType), of(itemType));
        } else {
            throw new IllegalArgumentException("Unknown collection type; " + collectionType);
        }

        return result;
    }

    public static ListType newListType(Class<?> itemType) {
        return new ListTypeImpl(of(List.class), of(itemType));
    }

    public static SetType newSetType(Class<?> itemType) {
        return new SetTypeImpl(of(Set.class), of(itemType));
    }

    public static MapType newMapType(Class<?> keyType, Class<?> valueType) {
        return new MapTypeImpl(of(keyType), of(valueType));
    }

    public static DataType of(Class<?> clz) {
        DataType result;

        if (Set.class.isAssignableFrom(clz)) {
            Class<?> itemType = TypeUtils.extractItemType(clz);
            result = new SetTypeImpl(new ScalarTypeImpl(clz), of(itemType));
        } else if (List.class.isAssignableFrom(clz)) {
            Class<?> itemType = TypeUtils.extractItemType(clz);
            result = new ListTypeImpl(new ScalarTypeImpl(clz), of(itemType));
        } else if (Map.class.isAssignableFrom(clz)) {
            Entry<Class<?>, Class<?>> e = TypeUtils.extractMapTypes(clz);
            result = new MapTypeImpl(of(e.getKey()), of(e.getValue()));
        } else {
            result = new ScalarTypeImpl(clz);
        }

        return result;
    }
}
