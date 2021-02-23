package org.aksw.commons.codec.entity.api;

/**
 * The base interface for a view on a codecs which are based
 * on getEncoder() and getDecoder() methods.
 *
 * @param <T>
 */
public interface EntityCodecIndirect<T> {
	EntityTransform<T> getEncoder();
	EntityTransform<T> getDecoder();
}
