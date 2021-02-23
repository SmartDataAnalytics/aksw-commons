package org.aksw.commons.codec.entity.util;

import java.util.function.Supplier;

import org.aksw.commons.codec.entity.api.EntityCodec;

public class EntityCodecUtils {
	/** Same as {@link #harmonize(Object, EntityCodec) except that the codec is taken from a supplier */
	public static <T> T harmonize(T entity, Supplier<? extends EntityCodec<T>> codecSupplier) {
		EntityCodec<T> codec = codecSupplier.get();
		T result = harmonize(entity, codec);
		return result;
	}

	/**
	 * Apply decoding to an entity (if applicable) then return the encoded entity
	 * Main use case is to remove non-canonical quotes from a strings and
	 * then return a new one with canonical quotes. 
	 */
	public static <T> T harmonize(T entity, EntityCodec<T> codec) {
		T canonicalEntity = entity;
		if (codec.canDecode(canonicalEntity)) {
			canonicalEntity = codec.decode(canonicalEntity);
		}
		T result = codec.encode(canonicalEntity);
		return result;
	}
}
