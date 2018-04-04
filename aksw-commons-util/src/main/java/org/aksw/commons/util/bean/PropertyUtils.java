package org.aksw.commons.util.bean;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PropertyUtils {

	/**
	 * Invoke the setter with the getter's value - unless the getter returns null.
	 * 
	 * @param setter
	 * @param getter
	 * @return
	 */
	public static <T> T applyIfPresent(Consumer<? super T> setter, Supplier<? extends T> getter) {
		T result = getter.get();
		Optional.ofNullable(result).ifPresent(setter::accept);
		return result;
	}

	/**
	 * If targetGetter yields null, invoke the targetSetter with the valueGetter's result.
	 * 
	 * @param targetSetter
	 * @param targetGetter
	 * @param valueGetter
	 * @return
	 */
	public static <T> T applyIfAbsent(Consumer<? super T> targetSetter, Supplier<? extends T> targetGetter, Supplier<? extends T> valueGetter) {
		T result = targetGetter.get();
		if(result == null) {
			result = valueGetter.get();
			targetSetter.accept(result);
		}

		return result;
	}

}
