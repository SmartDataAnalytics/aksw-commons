package org.aksw.commons.util.strings;

import java.util.Collection;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class CollectionFormatter {


    /**
     * doesn't print the last separator ;)
     * outputs:
     * 1<separator>2<separator>3<separator>4
     * @param collection
     * @param separator
     * @return
     */
    public static String collection2String(Collection collection, String separator){
        int i = 0;
        StringBuffer buf = new StringBuffer();
        for(Object s :collection){
            boolean last = (i==collection.size()-1);
                buf.append(s.toString());
            if(!last){
                buf.append(separator);
            }
            i++;
        }
        return buf.toString();
    }

}
