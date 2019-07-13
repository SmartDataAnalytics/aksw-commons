package org.aksw.commons.collections.generator;

import com.google.common.base.Converter;

public class GeneratorFromConverter<F, B, X extends Generator<B>>
	implements Generator<F>
{
	public GeneratorFromConverter(X delegate, Converter<B, F> converter) {
		super();
		this.delegate = delegate;
		this.converter = converter;
	}

	//protected Generator<B> getDelegate();
	protected X delegate;
	protected Converter<B, F> converter;

	@Override
	public F next() {
		B b = delegate.next();
		F f = converter.convert(b);
		return f;
	}
	@Override
	public F current() {
		B b = delegate.current();
		F f = converter.convert(b);
		return f;
	}

	@Override
	public Generator<F> clone() {
		return new GeneratorFromConverter<>(delegate.clone(), converter);
	}
}
