package org.aksw.commons.accessors;

import java.util.Collection;

import com.google.common.base.Converter;
import com.google.common.collect.Range;

public class CollectionAccessorFromConverter<B, F>
	implements CollectionAccessor<F>
{
	protected final CollectionAccessor<B> delegate;
	protected final Converter<B, F> converter;
	
	public CollectionAccessorFromConverter(CollectionAccessor<B> delegate, Converter<B, F> converter) {
		super();
		this.delegate = delegate;
		this.converter = converter;
	}

	@Override
	public Collection<F> get() {
		Collection<B> tmp = delegate.get();
		CollectionFromConverter<F, B, ?> result = new CollectionFromConverter<>(tmp, converter);
		return result;
		
	}	

	@Override
	public Range<Long> getMultiplicity() {
		Range<Long> result = delegate.getMultiplicity();
		return result;
	}
}
