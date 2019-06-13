package org.aksw.commons.collections.generator;

import java.util.Collection;
import java.util.function.Predicate;

public class GeneratorBlacklist<T>
	extends GeneratorForwarding<T>
{
	private Predicate<? super T> isBlacklisted;
	
	public GeneratorBlacklist(Generator<T> delegate, Predicate<? super T> isBlacklisted) {
	    super(delegate);
	    this.isBlacklisted = isBlacklisted;
	}
	
	@Override
	public GeneratorBlacklist<T> clone() {
	    Generator<T> clone = delegate.clone();
	    GeneratorBlacklist<T> result = new GeneratorBlacklist<>(clone, isBlacklisted);
	    return result;
	}
	
	@Override
	public T next() {
	    T result;
	    do {
	        result = delegate.next();
	    } while(isBlacklisted.test(result));
	
	    return result;
	}
	
	@Override
	public T current() {
	    T result = delegate.current();
	    return result;
	}
	
	public static <T> GeneratorBlacklist<T> create(Generator<T> generator, Predicate<? super T> isBlacklisted) {
		GeneratorBlacklist<T> result = new GeneratorBlacklist<T>(generator, isBlacklisted);
	    return result;
	}

	public static <T> GeneratorBlacklist<T> create(Generator<T> generator, Collection<?> blacklist) {
		GeneratorBlacklist<T> result = create(generator, blacklist::contains);
	    return result;
	}
	
//	@Override
//	public String toString() {
//	    return "current: " + generator.current() + ", blacklist: " + blacklist;
//	}
}