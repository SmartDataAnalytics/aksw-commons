package org.aksw.jena_sparql_api.txn;

import java.nio.file.Path;

public interface ResourceRepository<T> {

    Path getRootPath();
    String[] getPathSegments(T name);

}
