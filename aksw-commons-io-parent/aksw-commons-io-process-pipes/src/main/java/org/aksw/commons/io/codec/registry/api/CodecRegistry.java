package org.aksw.commons.io.codec.registry.api;

import com.google.common.collect.Multimap;

public interface CodecRegistry {
	public Multimap<String, CodecRegistration> nameToRegistrations();
}
