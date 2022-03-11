package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.Dialect;

public class DialectForwardingBase<D extends Dialect>
    implements DialectForwarding<D>
{
    protected D delegate;

    public DialectForwardingBase(D delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public D getDelegate() {
        return delegate;
    }
}
