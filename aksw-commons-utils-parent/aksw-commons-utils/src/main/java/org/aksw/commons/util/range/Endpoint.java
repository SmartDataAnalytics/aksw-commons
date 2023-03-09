package org.aksw.commons.util.range;

import com.google.common.collect.BoundType;

/** A poor-man's version of guava's internal Cut class */
public class Endpoint<T> {
    protected T value;
    protected BoundType boundType;

    public Endpoint(T value, BoundType boundType) {
        super();
        this.value = value;
        this.boundType = boundType;
    }

    public static <T> Endpoint<T> open(T value) {
        return new Endpoint<>(value, BoundType.OPEN);
    }

    public static <T> Endpoint<T> closed(T value) {
        return new Endpoint<>(value, BoundType.CLOSED);
    }

    public T getValue() {
        return value;
    }

    public BoundType getBoundType() {
        return boundType;
    }

    /** Create a new endpoint with the bound type toggled. Has no effect and returns 'this' if the value is null. */
    public Endpoint<T> toggleBoundType() {
        return value == null ? this : new Endpoint<>(value, BoundTypeUtils.toggle(boundType));
    }

    public boolean hasValue() {
        return value != null;
    }

    @Override
    public String toString() {
        return boundType + " " + value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boundType == null) ? 0 : boundType.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Endpoint other = (Endpoint) obj;
        if (boundType != other.boundType)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
