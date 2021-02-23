package org.aksw.commons.codec.string.impl;

import org.aksw.commons.codec.string.api.StringCodecDirectBase;

public class StringCodecPrefixAndSuffix
	implements StringCodecDirectBase
{
	protected final String prefix;
	protected final String suffix;
	
	public StringCodecPrefixAndSuffix(String prefix, String suffix) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	@Override
	public String encode(String entity) {
		String result = prefix + entity + suffix;
		return result;
	}

	@Override
	public boolean canDecode(String entity) {
		boolean result = entity.startsWith(prefix) && entity.endsWith(suffix);
		return result;
	}

	@Override
	public String decode(String entity) {
		if (!canDecode(entity)) {
			throw new IllegalArgumentException(String.format("Cannot decode %s with prefix=%s and suffix=%s", entity, prefix, suffix));
		}
		
		String result = entity.substring(prefix.length(), entity.length() - suffix.length());
		return result;
	}
	
	public static StringCodecPrefixAndSuffix create(String prefixAndSuffix) {
		return create(prefixAndSuffix, prefixAndSuffix);
	}

	public static StringCodecPrefixAndSuffix create(String prefix, String suffix) {
		return new StringCodecPrefixAndSuffix(prefix, suffix);
	}

	
}
