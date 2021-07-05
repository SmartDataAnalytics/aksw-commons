package org.aksw.commons.rx.range;

import java.io.IOException;

public interface KeyObjectStore {
    void put(Iterable<String> keySegments, Object obj) throws IOException;
    <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException;
}
