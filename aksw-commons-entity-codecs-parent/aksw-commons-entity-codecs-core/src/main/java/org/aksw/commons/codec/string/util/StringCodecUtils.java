package org.aksw.commons.codec.string.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.codec.entity.api.EntityCodec;
import org.aksw.commons.codec.entity.api.EntityTransform;
import org.aksw.commons.codec.entity.impl.EntityCodecImpl;
import org.aksw.commons.codec.entity.impl.EntityTransformCoalesce;
import org.aksw.commons.codec.string.api.StringCodec;
import org.aksw.commons.codec.string.impl.StringCodecPrefixAndSuffix;

public class StringCodecUtils {
	
	/**
	 * Create a string codec that encodes strings by prepending and appending a given quoteChar
	 * and which can decode by removing a certain set of given quote chars.
	 * 
	 * @param quoteChar
	 * @param dequoteChars
	 * @return
	 */
	public static EntityCodec<String> createCodec(String quoteChar, String ... dequoteChars) {
		StringCodec enquoter = StringCodecPrefixAndSuffix.create(quoteChar);

		List<EntityTransform<String>> dequoters = Stream.concat(
					Stream.of(enquoter.getDecoder()),
					Arrays.asList(dequoteChars).stream()
						.map(StringCodecPrefixAndSuffix::create)
						.map(EntityCodec::getDecoder))
				.collect(Collectors.toList());
		
		EntityTransformCoalesce<String> compoundTransform = EntityTransformCoalesce.create(dequoters);	
		EntityCodec<String> result = EntityCodecImpl.create(enquoter.getEncoder(), compoundTransform);

		return result;
	}
}
