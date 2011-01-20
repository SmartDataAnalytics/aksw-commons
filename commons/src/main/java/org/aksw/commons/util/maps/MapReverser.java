package org.aksw.commons.util.maps;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class MapReverser<K,V> {
    private static final Logger logger = Logger.getLogger(MapReverser.class);

    public Map<V, K> reverse( Map<K,V> map){
        Map<V,K> result = new HashMap<V,K>();
        for(K key:map.keySet()){
            if(result.get(key)!=null){
                String message = "ambigue entry found while reversing:\n";
                message +="was: "+key+" -> "+result.get(key)+"\n";
                message +="overwriting with: "+key+"->"+map.get(key)+"\n";
                logger.warn(message);
            }
            result.put(map.get(key), key);
        }
        return result;
    }

}
