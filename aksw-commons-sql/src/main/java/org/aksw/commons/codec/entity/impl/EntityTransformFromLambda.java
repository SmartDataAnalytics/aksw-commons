package org.aksw.commons.codec.entity.impl;

import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.codec.entity.api.EntityTransform;

public class EntityTransformFromLambda<T>
	implements EntityTransform<T>
{
	protected Predicate<? super T> canApply;
	protected Function<? super T, ? extends T> applyFn;

	public EntityTransformFromLambda(Predicate<? super T> canApply, Function<? super T, ? extends T> applyFn) {
		super();
		this.canApply = canApply;
		this.applyFn = applyFn;
	}

	@Override
	public boolean canApply(T entity) {
		boolean result = canApply.test(entity);
		return result;
	}

	@Override
	public T apply(T entity) {
		T result = applyFn.apply(entity);
		return result;
	}


	public static <T> EntityTransform<T> createAlwaysApplicable(Function<? super T, ? extends T> applyFn) {
		return create(entity -> true, applyFn);
	}

	public static <T> EntityTransform<T> create(Predicate<? super T> canApply, Function<? super T, ? extends T> applyFn) {
		return new EntityTransformFromLambda<>(canApply, applyFn);
	}

}
