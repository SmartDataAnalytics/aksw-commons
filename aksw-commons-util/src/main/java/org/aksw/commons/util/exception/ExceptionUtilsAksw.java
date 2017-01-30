package org.aksw.commons.util.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtilsAksw {

    public static Optional<Throwable> unwrap(Throwable given, Class<?> priority) {
    	Optional<Throwable> result = unwrap(given, Collections.singletonList(priority));
    	return result;
    }

    public static Optional<Throwable> unwrap(Throwable given, List<Class<?>> priorities) {

    	// Iterate the classes by priority and return the first match
    	Optional<Throwable> result = priorities.stream()
    		.map(priority -> ExceptionUtils.indexOfType(given, priority))
    		.filter(index -> index >= 0)
    		.map(ExceptionUtils.getThrowableList(given)::get)
    		.findFirst();

    	return result;
    }

}
