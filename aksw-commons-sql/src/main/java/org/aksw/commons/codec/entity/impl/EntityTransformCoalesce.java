package org.aksw.commons.codec.entity.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.aksw.commons.codec.entity.api.EntityTransform;

public class EntityTransformCoalesce<T>
	implements EntityTransform<T>
{
	protected Collection<? extends EntityTransform<T>> transforms;

	public EntityTransformCoalesce(Collection<? extends EntityTransform<T>> transforms) {
		super();
		this.transforms = transforms;
	}
	
	@Override
	public boolean canApply(T entity) {
		boolean result = findApplicableTransforms(entity).findAny().isPresent();
		return result;
	}

	@Override
	public T apply(T entity) {
		EntityTransform<T> xform = tryGetFirstApplicableTransform(entity)
				.orElseThrow(() -> new RuntimeException("No applicable transform found for " + entity));
		
		T result = xform.apply(entity);
		return result;
	}


	public Optional<EntityTransform<T>> tryGetFirstApplicableTransform(T entity) {
		return findApplicableTransforms(entity).findFirst();
	}

	
	public EntityTransform<T> getFirstApplicableTransform(T entity) {
		return tryGetFirstApplicableTransform(entity).orElse(null);
	}

//	@Override
	public Stream<EntityTransform<T>> findApplicableTransforms(T entity) {
		return transforms.stream()
				.filter(xform -> {
					boolean r = xform.canApply(entity);
					return r;
				})
				.map(x -> (EntityTransform<T>)x);
	}

	public static <T> EntityTransformCoalesce<T> create(Collection<? extends EntityTransform<T>> transforms) {
		return new EntityTransformCoalesce<>(transforms);
	}
}
