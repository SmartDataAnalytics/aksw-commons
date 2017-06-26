package org.aksw.commons.util.contextual;

import java.util.Optional;

/**
 * TODO This class looks similar to Jena's Polymorphic
 *
 *
 * Generic interface to access associated (context information / aspects / APIs / whatever to call it) of objects.
 * The default implementation of unwrap will try to cast object the method is called on
 * as the requested class.
 *
 *
 * @author raven
 *
 */
public interface Contextual {

	@SuppressWarnings("unchecked")
	default public <X> Optional<X> unwrap(Class<X> clazz, boolean reflexive) {
    	Optional<X> result;
    	if(reflexive && clazz.isAssignableFrom(this.getClass())) {
    		result = Optional.of((X)this);
    	} else {
    		if(this instanceof Delegated) {
    			Object delegate = ((Delegated<?>)this).delegate();
    			result = delegate instanceof Contextual
    					? ((Contextual)delegate).unwrap(clazz, true)
    					: Optional.empty();
    		} else {
    			result = Optional.empty();
    		}
    	}

    	return result;
	}
}
