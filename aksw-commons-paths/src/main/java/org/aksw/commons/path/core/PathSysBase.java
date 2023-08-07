package org.aksw.commons.path.core;

import java.util.Objects;

/**
 * A path where custom value can be set of the 'system' attribute.
 * This class is intended to mimic {@link java.nio.Path#getFileSystem} and can be used
 * to associate arbitrary user data with a path.
 */
public abstract class PathSysBase<T, P extends Path<T>, S>
    extends PathDelegateBase<T, P>
    implements PathSys<T, S>
{
    protected final S system;

    public PathSysBase(S system, Path<T> delegate) {
        super(delegate);
        this.system = system;
    }

    @Override
    public S getSystem() {
        return system;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(system);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathSysBase<?, ?, ?> other = (PathSysBase<?, ?, ?>) obj;
        return Objects.equals(system, other.system);
    }
}
