package org.aksw.commons.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * T is the Timestamp object (e.g. Date, Long, etc). Multiple keys may have the
 * same timestamp.
 *
 * At each time there may only be a single key.
 *
 *
 * @author raven JFreeChart
 * @param <K>
 */
public class CacheSet<K>
    implements Set<K>
// extends HashMap<K, V>
{
    private static final long	serialVersionUID	= -4277098373746171836L;

    private Map<K, Integer> keyToTime			= new HashMap<K, Integer>();
    private TreeMap<Integer, K> sortedTimes			= new TreeMap<Integer, K>();

    private int                 maxItemCount;
    private int					currentTime;									// This

    // Controls wheter the timestamp of a key which already exists should
    // be updated (true) or not (false).
    private boolean				allowRenewal;

    public Integer getKeyTime(K key)
    {
        return keyToTime.get(key);
    }

    /**
     * inclusive: wheter max distance is inclusive or exclusive. e.g. if
     * maxDistance is 10, and a distance is also 10, then inclusive: the item is
     * retained (10 is included) exclusive: the item is removed
     *
     */
    public CacheSet()
    {
        this.allowRenewal = true;
        setMaxItemCount(100);
    }

    public CacheSet(int maxItemCount, boolean allowRenewal)
    {
        this.allowRenewal = allowRenewal;
        setMaxItemCount(maxItemCount);
    }

    private Set<K> removeOutdated()
    {
        Set<K> removedKeys = new HashSet<K>();
        while (keyToTime.size() > maxItemCount)
            removedKeys.add(removeOldest());

        return removedKeys;
    }

    /**
     * Sets a new maximum distance.
     *
     * Returns all items that become outdated
     *
     * @param newMaxItemCount
     */
    public Set<K> setMaxItemCount(int newMaxItemCount)
    {
        maxItemCount = newMaxItemCount;

        return removeOutdated();
    }

    private K removeOldest()
    {
        Map.Entry<Integer, K> removeItem = sortedTimes.pollFirstEntry();

        K result = removeItem.getValue();
        keyToTime.remove(result);

        sortedTimes.remove(removeItem.getKey());

        return result;
    }

    private K removeOldestChecked()
    {
        if(keyToTime.size() > maxItemCount)
            return removeOldest();

        return null;
    }

    private Integer initKeyTime(K key)
    {
        keyToTime.put(key, currentTime);
        sortedTimes.put(currentTime, key);

        return currentTime++;
    }

    private void updateKeyTime(K key, Integer oldTime)
    {
        sortedTimes.remove(oldTime);
        initKeyTime(key);
    }

    @SuppressWarnings("unchecked")
    public boolean renew(Object key)
    {
        Integer keyTime = keyToTime.get(key);
        if (keyTime == null)
            return false;

        updateKeyTime((K)key, keyTime);
        return true;
    }

    /**
     * Adds an item, and returns an item that was removed - or null if no item
     * was removed
     * @param key
     * @return
     */
    public K addAndGetRemoved(K key)
    {
        add(key);

        return removeOldestChecked();
    }


    @Override
    public boolean add(K key)
    {
        boolean result = _add(key);
        removeOldestChecked();

        return result;
    }
    /**
     * Put operations are done for the current time
     *
     */
    public boolean _add(K key)
    {
        Integer keyTime = keyToTime.get(key);

        if (keyTime == null)
            initKeyTime(key);
        else if (allowRenewal)
            updateKeyTime(key, keyTime);

        return keyTime == null;
    }

    @Override
    public boolean addAll(Collection<? extends K> arg0)
    {
        boolean result = false;
        for (K item : arg0)
            result |= this.add(item);

        // TODO Auto-generated method stub
        return result;
    }

    @Override
    public void clear()
    {
        keyToTime.clear();
        sortedTimes.clear();
    }

    @Override
    public boolean contains(Object arg0)
    {
        return keyToTime.containsKey(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0)
    {
        boolean result = true;
        for (Object item : arg0)
            result &= this.contains(item);

        return result;
    }

    @Override
    public boolean isEmpty()
    {
        return keyToTime.isEmpty();
    }

    /**
     * FIXME Make sure that the iterator's remove method cannot be invoked
     * directly
     *
     */
    @Override
    public Iterator<K> iterator()
    {
        return keyToTime.keySet().iterator();
    }

    @Override
    public boolean remove(Object arg0)
    {
        Integer time = keyToTime.get(arg0);
        if (time == null)
            return false;

        sortedTimes.remove(time);
        keyToTime.remove(arg0);

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> items)
    {
        boolean result = false;
        for(Object item : items) {
            result = result || remove(item);
        }

        return result;
        //throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public boolean retainAll(Collection<?> arg0)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public int size()
    {
        return keyToTime.size();
    }

    @Override
    public Object[] toArray()
    {
        return keyToTime.keySet().toArray();
    }

    @Override
    public <X> X[] toArray(X[] arg0)
    {
        return keyToTime.keySet().toArray(arg0);
    }

    @Override
    public int hashCode()
    {
        return keyToTime.keySet().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return keyToTime.keySet().equals(o);
    }

    @Override
    public String toString()
    {
        return keyToTime.keySet().toString();
    }
}
