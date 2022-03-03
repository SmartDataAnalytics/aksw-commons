package org.aksw.commons.io.util;

import java.io.IOException;

/**
 * Interface meant for synchronizing the state of this object with some kind storage
 * (another object, a file, a database, etc).
 * 
 * @author raven
 *
 */
@FunctionalInterface
public interface Sync {
	void sync() throws IOException;
}
