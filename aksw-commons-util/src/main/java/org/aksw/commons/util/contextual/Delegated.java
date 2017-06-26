package org.aksw.commons.util.contextual;

/**
 * This is similar to Guava's ForwardingObject, except that
 * this thing is an interface and the delegate is publicly exposed.
 *
 * @author raven
 *
 * @param <T>
 */
public interface Delegated<T> {
	T delegate();
}
