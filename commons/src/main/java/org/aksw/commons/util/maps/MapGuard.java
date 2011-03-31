package org.aksw.commons.util.maps;

import java.util.HashMap;
import java.util.Map;

/**
* @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
*/
public abstract class MapGuard<K,V> {
    protected Map<K, V> map = null;

    public Map<K, V> getMap() {
        return new HashMap<K,V> (map);
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }

    
}
