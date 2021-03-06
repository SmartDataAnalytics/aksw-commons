package org.aksw.commons.collections.generator;

import java.util.function.Function;

public class GeneratorTransforming<F, B>
	implements Generator<F>
{
	protected Generator<B> delegate;
	protected Function<? super B, ? extends F> transform;
	
	public GeneratorTransforming(Generator<B> delegate, Function<? super B, ? extends F> transform) {
		super();
		this.delegate = delegate;
		this.transform = transform;
	}

	@Override
	public F next() {
		B b = delegate.next();
		F result = transform.apply(b);
		return result;
	}

	@Override
	public F current() {
		B b = delegate.current();
		F result = transform.apply(b);
		return result;
	}

	@Override
	public Generator<F> clone() {
		Generator<B> delegateClone = delegate.clone();
		return new GeneratorTransforming<>(delegateClone, transform);
	}

}
