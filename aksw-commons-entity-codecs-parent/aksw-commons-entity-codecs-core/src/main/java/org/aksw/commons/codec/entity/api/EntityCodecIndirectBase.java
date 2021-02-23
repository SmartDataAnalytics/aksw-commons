package org.aksw.commons.codec.entity.api;

/**
 * An entity codec provides the metohds getEncoder and getDecoder.
 * For convenience the interface extends {@link EntityCodecDirectBase} which
 * provides the simply encode and decode shortcuts.
 * 
 * @author Claus Stadler
 *
 * @param <T> The entity type on which encoding/decoding operates
 */
public interface EntityCodecIndirectBase<T>
	extends EntityCodec<T>
{	
	@Override
	default T encode(T entity) {
		EntityTransform<T> xform = getEncoder();
		T result = xform.apply(entity);
		return result;
	}

	@Override
	default T decode(T entity) {
		EntityTransform<T> xform = getDecoder();
		T result = xform.apply(entity);
		return result;
	}

	@Override
	default boolean canDecode(T entity) {
		EntityTransform<T> xform = getDecoder();
		boolean result = xform.canApply(entity);
		return result;
	}
}
