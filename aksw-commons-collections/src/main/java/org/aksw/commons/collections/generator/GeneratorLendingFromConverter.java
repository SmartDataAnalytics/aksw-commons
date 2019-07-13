package org.aksw.commons.collections.generator;

import com.google.common.base.Converter;

public class GeneratorLendingFromConverter<F, B, X extends GeneratorLending<B>>
	extends GeneratorFromConverter<F, B, X>
	implements GeneratorLending<F>
{
	public GeneratorLendingFromConverter(X delegate, Converter<B, F> converter) {
		super(delegate, converter);
	}

	@Override
	public boolean giveBack(F item) {
		B b = converter.reverse().convert(item);
		boolean result = delegate.giveBack(b);
		return result;
	}
}
