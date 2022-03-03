package org.aksw.commons.util.slot;

public interface SlottedBuilderDelegate<W, P>
    extends SlottedBuilder<W, P>
{
    SlottedBuilder<W, P> getDelegate();

    default Slot<P> newSlot() {
        return getDelegate().newSlot();
    }

    default W build() {
        return getDelegate().build();
    }
}
