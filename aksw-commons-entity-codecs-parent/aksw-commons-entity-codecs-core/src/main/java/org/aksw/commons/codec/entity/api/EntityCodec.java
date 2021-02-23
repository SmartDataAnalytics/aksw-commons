package org.aksw.commons.codec.entity.api;

/**
 * The EntityCodec API supports both the indirect and the direct views.
 * 
 * @author Claus Stadler
 *
 * @param <T>
 */
public interface EntityCodec<T>
	extends EntityCodecDirect<T>, EntityCodecIndirect<T>
{
}
