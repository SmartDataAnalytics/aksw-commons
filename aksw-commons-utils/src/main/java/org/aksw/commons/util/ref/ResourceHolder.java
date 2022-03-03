package org.aksw.commons.util.ref;

public interface ResourceHolder<T>
    extends AutoCloseable
{
    /**
     * A resource holder is valid as long as it was not closed.
     * Note, that the underlying resource may yet be closed.
     *
     * @return
     */
    boolean isValid();
    T get();
}
