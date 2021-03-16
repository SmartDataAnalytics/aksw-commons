package org.aksw.commons.codec.entity.api;

import java.util.function.Function;

public interface EntityTransform<T>
	extends Function<T, T> {
	boolean canApply(T entity);
	
	default T applyOrGetAsGiven(T entity) {
		boolean canApply = canApply(entity);
		T result = canApply
				? apply(entity)
				: entity;

		return result;
	}
}
