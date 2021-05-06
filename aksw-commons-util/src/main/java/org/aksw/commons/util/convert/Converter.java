package org.aksw.commons.util.convert;

import java.util.function.Function;

/** Interface to describe a conversion from one Java type to another */
public interface Converter {
	Class<?> getFrom();
	Class<?> getTo();
	Function<Object, Object> getFunction();
}