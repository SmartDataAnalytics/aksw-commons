package org.aksw.commons.collections;

import java.util.Collection;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/10/11
 *         Time: 12:03 PM
 */
public interface Descender<T>
{
	Collection<T> getDescendCollection(T item);
}