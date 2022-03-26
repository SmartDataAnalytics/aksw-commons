package org.aksw.commons.model.csvw.domain.impl;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;

public class DialectMutableForwardingBase<D extends DialectMutable>
    extends DialectForwardingBase<D>
    implements DialectMutableForwarding<D>
{

    public DialectMutableForwardingBase(D delegate) {
        super(delegate);
    }

    @Override
    public D getDelegate() {
        return delegate;
    }
}
