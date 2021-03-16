package org.aksw.commons.util.delegate;

import java.util.Optional;

/**
 * Generic interface to access associated (context information / aspects / APIs / whatever to call it) of objects.
 * The default implementation of unwrap will try to cast object the method is called on
 * as the requested class.
 *
 *
 * @author raven
 *
 */
public interface Unwrappable {

	@SuppressWarnings("unchecked")
	default public <X> Optional<X> unwrap(Class<X> clazz, boolean reflexive) {
    	Optional<X> result;
    	if(reflexive && clazz.isAssignableFrom(this.getClass())) {
    		result = Optional.of((X)this);
    	} else {
    		if(this instanceof Delegated) {
    			Object delegate = ((Delegated<?>)this).delegate();
    			result = delegate instanceof Unwrappable
    					? ((Unwrappable)delegate).unwrap(clazz, true)
    					: Optional.empty();
    		} else {
    			result = Optional.empty();
    		}
    	}

    	return result;
	}
}
