package org.aksw.commons.util.validation;

import org.apache.commons.collections15.map.LRUMap;
import org.apache.commons.validator.UrlValidator;

/**
 * A wrapper for the apache UrlValidator which caches
 * results in a least-recently-used (LRU) map.
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class UrlValidatorCached
{
	private static final int DEFAULT_CACHE_SIZE = 10000;

	private UrlValidator urlValidator = new UrlValidator();

	private LRUMap<String, Boolean> cache;

	public UrlValidatorCached() {
		this(DEFAULT_CACHE_SIZE);
	}

	public UrlValidatorCached(int cacheSize) {
		cache = new LRUMap<String, Boolean>(cacheSize);
	}

	public boolean isValid(String uri) {
		Boolean result = cache.get(uri);
		if(result == null) {
			result = urlValidator.isValid(uri);

			cache.put(uri, result);
		}

		return result;
	}

}