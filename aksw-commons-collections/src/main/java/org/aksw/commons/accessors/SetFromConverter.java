package org.aksw.commons.accessors;

import java.util.Set;

import com.google.common.base.Converter;

public class SetFromConverter<F, B>
	extends CollectionFromConverter<F, B, Set<B>>
	implements Set<F>
{
	public SetFromConverter(Set<B> backend, Converter<B, F> converter) {
		super(backend, converter);
	}
}
