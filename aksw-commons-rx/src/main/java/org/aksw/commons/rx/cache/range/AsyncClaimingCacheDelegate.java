package org.aksw.commons.rx.cache.range;

public interface AsyncClaimingCacheDelegate<K, V>
    extends AsyncClaimingCache<K, V>
{
    AsyncClaimingCache<K, V> getDelegate();
}
