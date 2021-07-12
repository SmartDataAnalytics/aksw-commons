package org.aksw.commons.util.slot;

public interface SlotSource<P> {
    Slot<P> newSlot(/* R requester */);
}
