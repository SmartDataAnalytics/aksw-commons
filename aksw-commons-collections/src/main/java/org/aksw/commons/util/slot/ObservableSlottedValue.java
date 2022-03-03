package org.aksw.commons.util.slot;

import org.aksw.commons.collection.observable.ObservableValue;

public interface ObservableSlottedValue<W, P>
    extends SlottedBuilder<W, P>, ObservableValue<W>
{

}
