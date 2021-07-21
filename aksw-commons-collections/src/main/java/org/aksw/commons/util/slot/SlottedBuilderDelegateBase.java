package org.aksw.commons.util.slot;

public class SlottedBuilderDelegateBase<W, P>
    implements SlottedBuilderDelegate<W, P>
{
    protected SlottedBuilder<W, P> delegate;

    public SlottedBuilderDelegateBase(SlottedBuilder<W, P> delegate) {
        super();
        this.delegate = delegate;
    }

    public SlottedBuilder<W, P> getDelegate() {
        return delegate;
    }
}
