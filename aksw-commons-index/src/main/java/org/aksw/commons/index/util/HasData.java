package org.aksw.commons.index.util;

public interface HasData<T> {
    T getData();
    HasData<T> setData(T data);
}
