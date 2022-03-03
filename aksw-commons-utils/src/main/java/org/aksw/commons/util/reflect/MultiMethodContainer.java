package org.aksw.commons.util.reflect;

import java.util.List;

/**
 * A container where multiple objects/classes can register their methods.
 * The invoke method will then search for the most appropriate method.
 *
 * The generic indicates the return type candidate methods must be covariant with
 *
 * User: raven
 * Date: 6/11/11
 * Time: 9:33 AM
 *
 *
 *
 * What I want at some point in time is some kind of a rule container:
 * "Search for the method in this object, if in some other no match was found"
 * or
 * "If some condition is satisfied, (e.g. function name equals "foo")
 * then let this method handle that
 *
 */
public class MultiMethodContainer<T> {
    public void addStatic(Class<?> clazz) {

    }

    public void add(Object object) {
        
    }

    public void removeStatic(Class<?> clazz) {
        
    }

    public void remove(Object object) {

    }

    public T invoke(String name, List<Object> args) {
        return null;
    }

    public T invoke(String name, Object[] args) {
        return null;
    }
}
