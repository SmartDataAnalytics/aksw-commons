package org.aksw.commons.collections.generator;

import java.util.function.Function;

public class GeneratorFromFunction<T>
	implements Generator<T>
{
	protected T current;
	protected Function<? super T, ? extends T> inc; 
	
	public GeneratorFromFunction(T current, Function<? super T, ? extends T> inc) {
		super();
		this.current = current;
		this.inc = inc;
	}

	@Override
	public T next() {
		current = inc.apply(current);
		return current;
	}

	@Override
	public T current() {
		return current;
	}

	@Override
	public Generator<T> clone() {
		return new GeneratorFromFunction<T>(current, inc);
	}
	
	public static <T> GeneratorFromFunction<T> create(T current, Function<? super T, ? extends T> inc) {
		return new GeneratorFromFunction<>(current, inc);
	}
	
	public static GeneratorFromFunction<Integer> createInt() {
		return createInt(0);
	}
	
	public static GeneratorFromFunction<Integer> createInt(int initialNext) {
		return create(initialNext - 1, Math::incrementExact);
	}
}
