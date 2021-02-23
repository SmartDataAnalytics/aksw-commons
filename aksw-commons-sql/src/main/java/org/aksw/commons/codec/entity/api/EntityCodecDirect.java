package org.aksw.commons.codec.entity.api;

/**
 * The base interface for a view on a codecs which are based
 * on encode(), decode() and canDecode() methods.
 * 
 * Concrete implementations typically want to implicitly implement this interface
 * by implementing {@link EntityCodecDirectBase}.
 *
 * @param <T>
 */
public interface EntityCodecDirect<T> {
	T encode(T entity);

	boolean canDecode(T entity);
	T decode(T entity);
}
