package org.aksw.commons.util.collections;

import org.apache.commons.collections15.Transformer;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:48:49 PM
 */
public class StringTransformer<T>
    implements Transformer<T, String>
{
    public String transform(T item) {
        return item.toString();
    }
}