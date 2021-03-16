package org.aksw.commons.codec.string.impl;

import java.util.regex.Pattern;

import org.aksw.commons.codec.string.api.StringCodecDirectBase;

/**
 * StringCodec that prepends and appends a quote char to a given string
 * and also escapes any occurrence of the quote char within the string with an escape char.
 * 
 * @author raven
 *
 */
public class StringCodecQuote
	implements StringCodecDirectBase
{
	protected final char quoteChar;
	protected final char escapeChar;
	
	public StringCodecQuote(char quoteChar, char escapeChar) {
		super();
		this.quoteChar = quoteChar;
		this.escapeChar = escapeChar;
	}

	public char getQuoteChar() {
		return quoteChar;
	}

	public char getEscapeChar() {
		return escapeChar;
	}


	@Override
	public String encode(String entity) {
		String escaped = entity
				.replace("" + escapeChar, "" + escapeChar + escapeChar)
				.replace("" + quoteChar, "" + escapeChar + quoteChar);
		
		String result = quoteChar + escaped + quoteChar;
		return result;
	}

	@Override
	public boolean canDecode(String entity) {
		boolean result = entity.startsWith("" + quoteChar) && entity.endsWith("" + quoteChar);
		return result;
	}

	@Override
	public String decode(String entity) {
		if (!canDecode(entity)) {
			throw new IllegalArgumentException(String.format("Cannot decode %s with quote char %c and escape char", entity, quoteChar, escapeChar));
		}

		String result = entity.substring(1, entity.length() - 1)
				.replaceAll(Pattern.quote("" + escapeChar) + "(.)", "$1");
		return result;
	}
	

	public static StringCodecQuote create(char quoteChar) {
		return create(quoteChar, '\\');
	}

	public static StringCodecQuote create(char quoteChar, char escapeChar) {
		return new StringCodecQuote(quoteChar, escapeChar);
	}
	
}
