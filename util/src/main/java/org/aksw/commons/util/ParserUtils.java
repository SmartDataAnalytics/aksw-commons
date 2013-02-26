package org.aksw.commons.util;

import org.aksw.commons.util.reflect.Caster;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


class DatatypeReport {
    private int successCount = 0;
    private int totalCount = 0;

    private Class<?> datatype;
    private Range<Object> valueRange;

    public DatatypeReport() {
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }




    public int getErrorCount() {
        return totalCount - successCount;
    }
}


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/26/11
 *         Time: 1:02 PM
 */
public class ParserUtils {
    public static <T> T tryParse(Class<T> clazz, String str) {
        try {
            return (T) Caster.tryCast(str, clazz);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Analyze the key's values and return a report about the datatype
     * and the value range
     *
     *
     * @param map
     * @param <K>
     * @param <V>
     */
    public static <K, V> void analyzeMap(Map<K, V> map) {
        List<? extends Class<? extends Serializable>> datatypes = Arrays.asList(Boolean.class, Integer.class, Double.class);



        for(Map.Entry<K, V> entry : map.entrySet()) {
            Object value = entry.getValue();
        }
    }
}
