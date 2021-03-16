package org.aksw.commons.util.delegate;

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
