package org.aksw.commons.sql.codec.playground.main;

import java.util.Arrays;

import org.aksw.commons.codec.entity.api.EntityCodec;
import org.aksw.commons.codec.entity.impl.EntityCodecImpl;
import org.aksw.commons.codec.entity.impl.EntityTransformCoalesce;
import org.aksw.commons.codec.string.api.StringCodec;
import org.aksw.commons.codec.string.impl.StringCodecPrefixAndSuffix;
import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.impl.SqlCodecImpl;


public class MainPlaygroundCodec {
	public static void main(String[] args) {
		StringCodec enquoter = StringCodecPrefixAndSuffix.create("\"");

		StringCodec dequoter1 = StringCodecPrefixAndSuffix.create("\"");
		StringCodec dequoter2 = StringCodecPrefixAndSuffix.create("'");
		StringCodec dequoter3 = StringCodecPrefixAndSuffix.create("`");
		
		EntityTransformCoalesce<String> compoundTransform = EntityTransformCoalesce.create(Arrays.asList(
			dequoter1.getDecoder(),
			dequoter2.getDecoder(),
			dequoter3.getDecoder()
		));
		
		EntityCodec<String> idCodec = EntityCodecImpl.create(enquoter.getEncoder(), compoundTransform);
		
		System.out.println(idCodec.encode("test"));
		// output: "test"
		
		System.out.println(idCodec.encode(idCodec.encode("test")));
		// output: ""test""

		System.out.println(idCodec.decode("'test'"));
		// output: test

		System.out.println(idCodec.decode("`test`"));
		// output: test

		System.out.println(idCodec.encode(idCodec.decode("`test`")));
		// output: "test"

		
		// Expected IllegalArgumentException if the argument cannot be decoded:
		// System.out.println(idCodec.decode("test"));
		
		
		StringCodec stringLiteralEnquoter = StringCodecPrefixAndSuffix.create("'");
		EntityCodec<String> stringLiteralCodec = EntityCodecImpl.create(
				stringLiteralEnquoter.getEncoder(),
				compoundTransform);

		// Create an sqlCodec that uses double quotes for identifier and
		// single quotes for strings
		SqlCodec sqlCodec = SqlCodecImpl.create(idCodec, stringLiteralCodec);
		
		System.out.println(sqlCodec.forTableName().encode("tableName"));
		// output: "tableName"
		
		
		System.out.println(sqlCodec.forStringLiteral().encode("I am a string"));
		// output: 'I am a string'
	}
}
