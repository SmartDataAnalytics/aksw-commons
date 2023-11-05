package org.aksw.commons.path.core;

public class PathOpsWrapperBase<T, P extends Path<T>>
    implements PathOpsWrapper<T, P>
{
    protected PathOps<T, P> delegate;

    public PathOpsWrapperBase(PathOps<T, P> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public PathOps<T, P> getDelegate() {
        return delegate;
    }
}
