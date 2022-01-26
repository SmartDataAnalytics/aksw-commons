package org.aksw.commons.accessors;

import com.google.common.base.Converter;

public interface SingleValuedAccessor<T> {
	T get();
	void set(T value);
	
	/** Return a provided default value in case get() returns null. */
	default T getOrDefault(T defaultValue) {
		T result = get();
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}
	
	default <U> SingleValuedAccessor<U> convert(Converter<T, U> converter) {
		return new SingleValuedAccessorConverter<>(this, converter);
	}
}
