package org.aksw.commons.util.slot;

public class SlotDelegateBase<T>
    implements SlotDelegate<T>
{
    protected Slot<T> delegate;

    public SlotDelegateBase(Slot<T> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Slot<T> getDelegate() {
        return delegate;
    }
}
