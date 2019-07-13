package org.aksw.commons.collections.generator;

import java.util.function.Function;

import com.google.common.base.Converter;

public class Converters {
	public static Converter<Integer, String> prefixIntToStr(String prefix) {
		return prefix(prefix, Integer::parseInt, x -> Integer.toString(x));
	}

	public static <T> Converter<T, String> prefix(
			String prefix,
			Function<String, T> parse,
			Function<T, String> unparse) {
		
		int n = prefix.length();

		return new Converter<T, String>() {

			@Override
			protected String doForward(T a) {
				String str = unparse.apply(a);
				return prefix + str;
			}

			@Override
			protected T doBackward(String b) {
				T result;
				if(b.startsWith(prefix)) {
					String substr = b.substring(n, b.length());
					result = parse.apply(substr);
				} else {
					// TODO Raise exception?
					result = null;
				}

				return result;
			}
			
		};
	}
}