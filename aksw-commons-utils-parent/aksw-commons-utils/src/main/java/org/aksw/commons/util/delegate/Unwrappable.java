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
                result = unwrap(delegate, clazz, true);
//                result = delegate instanceof Unwrappable
//                        ? ((Unwrappable)delegate).unwrap(clazz, true)
//                        : Optional.empty();
            } else {
                result = Optional.empty();
            }
        }

        return result;
    }

    public static <X> Optional<X> unwrap(Object o, Class<X> clazz, boolean reflexive) {
        Optional<X> result;
        Class<?> oClass = o.getClass();
        if(reflexive && clazz.isAssignableFrom(oClass)) {
            result = Optional.of((X)o);
        } else {
            if (o instanceof Unwrappable) {
                result = ((Unwrappable)o).unwrap(clazz, true);
            } else if(o instanceof Delegated) {
                Object delegate = ((Delegated<?>)o).delegate();
                result = unwrap(delegate, clazz, true);
            } else {
                result = Optional.empty();
            }
        }
        return result;
    }
}
