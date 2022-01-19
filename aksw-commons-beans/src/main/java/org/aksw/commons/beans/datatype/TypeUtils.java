package org.aksw.commons.beans.datatype;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class TypeUtils {
    // Getter must be no-arg methods, whose result type is either a subclass of
    // RDFNode or a type registered at jena's type factory

    public static List<Class<?>> extractItemTypes(Type genericType) {
        List<Class<?>> result = new ArrayList<>();
        if(genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
            for( java.lang.reflect.Type argType : types) {
                if(argType instanceof Class) {
                    result.add((Class<?>)argType);
                } else if(argType instanceof WildcardType) {
                    // TODO We should take bounds into account
                    result.add(Object.class);
                } else {
                    result.add(null);
                    //throw new RuntimeException("Don't know how to handle " + argType);
                }
            }
        }
        return result;
    }

    public static Entry<Class<?>, Class<?>> extractMapTypes(Type genericType) {
        Entry<Class<?>, Class<?>> result = null;
        List<Class<?>> types = extractItemTypes(genericType);
        if(types.size() == 2) {
            Class<?> keyType = types.get(0);
            Class<?> valueType = types.get(1);
            if(keyType != null && valueType != null) {
                result = Maps.immutableEntry(keyType, valueType);
            } else {
                throw new RuntimeException("Don't know how to handle " + genericType);
            }
        }
        return result;
    }

    public static Class<?> extractItemType(Type genericType) {
        Class<?> result = null;
        List<Class<?>> types = extractItemTypes(genericType);
        if(types.size() == 1) {
            Class<?> argType = types.get(0);
            if(argType != null) {
                result = argType;
            } else {
                throw new RuntimeException("Don't know how to handle " + genericType);
            }
        }

//        if(genericType instanceof ParameterizedType) {
//            ParameterizedType pt = (ParameterizedType)genericType;
//            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
//            if(types.length == 1) {
//            	Type argType = types[0];
//            	if(argType instanceof Class) {
//            		result = (Class<?>)argType;
//            	} else if(argType instanceof WildcardType) {
//            		// TODO We should take bounds into account
//            		result = Object.class;
//            	} else {
//            		throw new RuntimeException("Don't know how to handle " + argType);
//            	}
//            }
//        }

        return result;
    }
}
