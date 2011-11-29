package org.aksw.commons.sparql.api.cache.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:39 PM
 */
public class CacheEntry {
    private long timestamp;
    private long lifespan;
    private InputStreamProvider inputStreamProvider;

    public CacheEntry(long timestamp, long lifespan, InputStreamProvider inputStreamProvider) {
        this.timestamp = timestamp;
        this.lifespan = lifespan;
        this.inputStreamProvider = inputStreamProvider;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLifespan() {
        return lifespan;
    }

    public InputStreamProvider getInputStreamProvider() {
        return inputStreamProvider;
    }
}
