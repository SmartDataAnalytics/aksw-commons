package org.aksw.commons.accessors;

import java.util.Collection;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

/**
 * Not sure if this this should really inherit from single valued accessor
 * -> Maybe rename to BasicAccessor or SimpleAccessor
 * 
 * 
 * @author raven May 2, 2018
 *
 * @param <B>
 */
public interface CollectionAccessor<B>
//	extends SingleValuedAccessor<Collection<T>>
{
	/**
	 * Returns the multiplicity of the underlying collection
	 * This is the minimum and maximum number of items this collection may hold.
	 * If a minimum exists, it is assumed that the collection's iterator yields those immutable entries first.
	 * 
	 * @return
	 */
	Range<Long> getMultiplicity();
	Collection<B> get();
	
	default <F> CollectionAccessor<F> convert(Converter<B, F> converter) {
		return new CollectionAccessorFromConverter<>(this, converter);
	}

}
