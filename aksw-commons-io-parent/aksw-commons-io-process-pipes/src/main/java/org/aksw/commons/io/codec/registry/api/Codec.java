package org.aksw.commons.io.codec.registry.api;

import org.aksw.commons.io.process.pipe.PipeTransform;

public interface Codec {
	PipeTransform encoder();
	PipeTransform decoder();
}
