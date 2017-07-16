package org.aksw.commons.collections.cache;

import java.util.List;

public interface Cache<T>
    extends List<T>, AutoCloseable
{
    int getCurrentSize();
    //List<T> getData();
    boolean isAbandoned();
    boolean isComplete();

    void setAbandoned();
    void setComplete();
}
