package org.aksw.commons.sql.codec.util;

import org.aksw.commons.codec.entity.api.EntityCodec;
import org.aksw.commons.codec.string.impl.StringCodecPrefixAndSuffix;
import org.aksw.commons.codec.string.util.StringCodecUtils;
import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.impl.SqlCodecImpl;

/**
 * Factory methods for typical SqlCodecs
 * 
 * @author Claus Stadler
 */
public class SqlCodecUtils {
	
	/**
	 * Create an sql codec.
	 * The only supported dequote char for string literals is its quote char.
	 * Multiple dequote chars are supported for identifiers.
	 */
	public static SqlCodec createSqlCodec(String stringLiteralQuoteChar, String idQuoteChar, String ... idDequoteChars) {
		EntityCodec<String> idCodec = StringCodecUtils.createCodec(idQuoteChar, idDequoteChars);
		EntityCodec<String> stringLiteralCodec = StringCodecPrefixAndSuffix.create(stringLiteralQuoteChar);
		
		SqlCodec result = SqlCodecImpl.create(idCodec, stringLiteralCodec);
		return result;
	}

	/** Create an sql codec that encodes with double quotes */
	public static SqlCodec createSqlCodecDefault() {
		SqlCodec result = SqlCodecUtils.createSqlCodec("'", "\"", "`", "'");
		return result;
	}

	public static SqlCodec createSqlCodecDoubleQuotes() {
		SqlCodec result = SqlCodecUtils.createSqlCodec("'", "\"");
		return result;
	}

	/** Create an sql codec suitable for use with apache spark - uses backticks for escaping sql identifiers */
	public static SqlCodec createSqlCodecForApacheSpark() {
		SqlCodec result = SqlCodecUtils.createSqlCodec("'", "`", "\"", "'");
		return result;
	}

}
