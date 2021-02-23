package org.aksw.commons.codec.entity.impl;

import org.aksw.commons.codec.entity.api.EntityCodecIndirectBase;
import org.aksw.commons.codec.entity.api.EntityTransform;

public class EntityCodecImpl<T>
	implements EntityCodecIndirectBase<T>
{
	protected EntityTransform<T> encoder;
	protected EntityTransform<T> decoder;
	
	public EntityCodecImpl(EntityTransform<T> encoder, EntityTransform<T> decoder) {
		super();
		this.encoder = encoder;
		this.decoder = decoder;
	}

	@Override
	public EntityTransform<T> getEncoder() {
		return encoder;
	}

	@Override
	public EntityTransform<T> getDecoder() {
		return decoder;
	}
	
	public static <T> EntityCodecIndirectBase<T> create(EntityTransform<T> encoder, EntityTransform<T> decoder) {
		return new EntityCodecImpl<>(encoder, decoder);
	}
}
