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
	
	
	/** Same as {@link #reencode(Object, EntityCodec, EntityCodec) except that the codec is taken from a supplier */
	public static <T> T reencode(
			T entity,
			Supplier<? extends EntityCodec<T>> decodecSupplier,
			Supplier<? extends EntityCodec<T>> encodecSupplier) {
		EntityCodec<T> decodec = decodecSupplier.get();
		EntityCodec<T> encodec = encodecSupplier.get();
		T result = reencode(entity, decodec, encodec);
		return result;
	}

	/**
	 * If the entity can be decoded with 'decoder' then do so and apply
	 * encoder.encode afterwards.
	 * If the entity cannot be decoded then return it unchanged.
	 * Useful to change e.g. quoting from double quotes to backticks
	 * 
	 */
	public static <T> T reencode(T entity, EntityCodec<T> decodec, EntityCodec<T> encodec) {
		T result;
		if (decodec.canDecode(entity)) {
			T tmp = decodec.decode(entity);
			result = encodec.encode(tmp);
		} else {
			result = entity;
		}
		return result;
	}

}
