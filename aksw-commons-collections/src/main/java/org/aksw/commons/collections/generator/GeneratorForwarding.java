package org.aksw.commons.collections.generator;

public abstract class GeneratorForwarding<T>
	implements Generator<T>
{
	protected Generator<T> delegate;
	
	public GeneratorForwarding(Generator<T> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public T next() {
		T result = delegate.next();
		return result;
	}

	@Override
	public T current() {
		T result = delegate.current();
		return result;
	}
	
	public abstract GeneratorForwarding<T> clone();
}
