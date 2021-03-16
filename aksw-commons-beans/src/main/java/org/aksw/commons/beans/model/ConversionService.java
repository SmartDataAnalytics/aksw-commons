package org.aksw.commons.beans.model;

public interface ConversionService {
	<T> boolean canConvert(Class<?> sourceType, Class<T> targetType);
	<T> T convert(Object source, Class<T> targetType);
}
