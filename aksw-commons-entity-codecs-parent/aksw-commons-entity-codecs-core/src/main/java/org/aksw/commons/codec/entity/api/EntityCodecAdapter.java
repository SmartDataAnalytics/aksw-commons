package org.aksw.commons.codec.entity.api;

import org.aksw.commons.codec.entity.impl.EntityTransformFromLambda;

public class EntityCodecAdapter<T, C extends EntityCodecDirect<T>>
	implements EntityCodecIndirectBase<T>
{
	protected C codecCore;
	
	public EntityCodecAdapter(C core) {
		super();
		this.codecCore = core;
	}

	@Override
	public EntityTransform<T> getEncoder() {
		return EntityTransformFromLambda.createAlwaysApplicable(codecCore::encode);
	}

	@Override
	public EntityTransform<T> getDecoder() {
		return EntityTransformFromLambda.create(codecCore::canDecode, codecCore::decode);
	}
}
