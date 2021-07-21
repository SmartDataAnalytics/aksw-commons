package org.aksw.commons.accessors;

import java.io.Serializable;

public class SingleValuedAccessorDirect<T>
    implements SingleValuedAccessor<T>, Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected T value;


    public SingleValuedAccessorDirect() {
        this(null);
    }

    public SingleValuedAccessorDirect(T value) {
        super();
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }


}
