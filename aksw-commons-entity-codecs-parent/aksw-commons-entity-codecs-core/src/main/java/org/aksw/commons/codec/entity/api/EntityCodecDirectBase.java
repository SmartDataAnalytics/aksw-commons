package org.aksw.commons.codec.entity.api;

/**
 * A basic codec for entities (rather than streams).
 * Typically for use with Strings.
 */
public interface EntityCodecDirectBase<T>
	extends EntityCodec<T>
{	
	default EntityCodecIndirectBase<T> asEntityCodec() {
		return new EntityCodecAdapter<>(this);
	}
	
	@Override
	default EntityTransform<T> getEncoder() {
		return asEntityCodec().getEncoder();
	}
	
	@Override
	default EntityTransform<T> getDecoder() {
		return asEntityCodec().getDecoder();
	}
}
