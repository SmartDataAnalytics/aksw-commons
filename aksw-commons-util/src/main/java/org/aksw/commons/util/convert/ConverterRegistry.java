package org.aksw.commons.util.convert;

import java.lang.reflect.Method;
import java.util.function.Function;

public interface ConverterRegistry {
	
	Converter getConverter(Class<?> from, Class<?> to);
	void register(Converter converter);
	
	default <R, J> ConverterRegistry register(
			Class<R> src,
			Class<J> tgt,
			Function<? super R, ? extends J> srcToTgt) {
		Converter converter = ConverterImpl.create(src, tgt, srcToTgt);
		register(converter);
		
		return this;
	}
	
	default <R, J> ConverterRegistry register(
			Class<R> src,
			Class<J> tgt,
			Function<? super R, ? extends J> srcToTgt,
			Function<? super J, ? extends R> tgtToSrc) {
		register(src, tgt, srcToTgt);
		register(tgt, src, tgtToSrc);
		
		return this;
	}

	default ConverterRegistry register(Method method) {
		Converter converter = ConverterImpl.create(method);
		register(converter);
		
		return this;
	}

}
