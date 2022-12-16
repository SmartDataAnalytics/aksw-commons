package org.aksw.commons.util.memoize;

import java.util.Map;

public interface Memoized<I, O> {
	/** Return a reference or copy to the memoized entries */
	Map<I, O> getCache();

	/** Clear the cache */
	void clearCache();
}
