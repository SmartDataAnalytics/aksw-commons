package org.aksw.commons.txn.impl;

import java.nio.file.Path;

/**
 * Interface for mapping an entity to a folder w.r.t. a base path.
 * 
 * @author raven
 *
 * @param <T>
 */
public interface PathMapper<T> {
	Path resolve(Path basePath, T entity);
}
