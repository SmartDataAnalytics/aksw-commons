package org.aksw.commons.collections.cache;

import java.util.Collection;
import java.util.List;

public interface Cache<T>
    extends Collection<T>
{
    List<T> getData();
    boolean isAbandoned();
    boolean isComplete();

    void setComplete();
}
