package org.aksw.commons.rx.cache.range;

import org.aksw.commons.cache.async.AsyncClaimingCache;

public interface AsyncClaimingCacheDelegate<K, V>
    extends AsyncClaimingCache<K, V>
{
    AsyncClaimingCache<K, V> getDelegate();
}
