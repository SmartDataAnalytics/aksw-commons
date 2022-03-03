package org.aksw.commons.txn.impl;

import java.nio.file.Path;

public interface ResourceRepository<T> {

    Path getRootPath();
    String[] getPathSegments(T name);

}
