package org.aksw.commons.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.util.maps.MapGuard;
import org.aksw.commons.util.maps.MapReverser;
import org.apache.log4j.Logger;

/**
 * A class for handling prefixes
 */
public class Prefixer {
    private static final Logger logger = Logger.getLogger(Prefixer.class);

    private String base = "";
    private Map<String, String> prefixToNamespace = new HashMap<String, String>();
    private MapGuard<String, String> namespaceToPrefix = new MapGuard<String, String>() {
        @Override
        public Map<String, String> getMap() {
            if (this.map == null) {
                namespaceToPrefix.setMap(new MapReverser<String, String>().reverse(prefixToNamespace));
            }
            return new HashMap<String, String>(map);
        }
    };


    public Prefixer() {}

    public Prefixer(String base, Map<String, String> prefixToNamespace, boolean appendPopularPrefixes) {
        this();
        this.base = base;
        if (appendPopularPrefixes) {
            prefixToNamespace.putAll(theMostPopulars());
        }
        setPrefixToNamespace(prefixToNamespace);
    }


    private Map<String, String> theMostPopulars() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("owl", "http://www.w3.org/2002/07/owl#");
        m.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.put("dbo", "http://dbpedia.org/ontology/");
        m.put("dbp", "http://dbpedia.org/property/");
        m.put("foaf", "http://xmlns.com/foaf/0.1/");
        m.put("skos", "http://www.w3.org/2004/02/skos/core#");
        m.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.put("dc", "http://purl.org/dc/elements/1.1/");
        m.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        return m;
    }

    public void addPrefix(String prefix, String namespace) {
        this.prefixToNamespace.put(prefix, namespace);
        namespaceToPrefix.setMap(null);
    }

    public void setPrefixToNamespace(Map<String, String> prefixToNamespace) {
        this.prefixToNamespace = prefixToNamespace;
        namespaceToPrefix.setMap(null);

    }

    public Map<String, String> getPrefixToNamespace() {
        return prefixToNamespace;
    }

    public Map<String, String> getNamespaceToPrefix() {
        return namespaceToPrefix.getMap();
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    /**
     * TODO base is missing
     *
     * @return
     */

    public String toSparqlPrefix() {
        StringBuffer b = new StringBuffer();
        for (String key : prefixToNamespace.keySet()) {
            String value = prefixToNamespace.get(key);
            b.append("PREFIX " + key + ": <" + value + ">\n");
        }
        return b.toString();
    }


    public String nicen(String uri) {
        String name = stripNamespace(uri);
        try {
            name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }

        name = name.replaceAll("_", " ");
        return name;
    }

    /**
     * strips the namespace, either by base or known prefix or a heuristic
     *
     * @param uri
     * @return Ths stripped uri
     */
    public String stripNamespace(String uri) {
        String name = uri.replace(base, "");
        for (String s : namespaceToPrefix.getMap().keySet()) {
            name = name.replace(s, "");
        }
        if (name.equals(uri)) {
            int hash = name.lastIndexOf('#');
            int slash = name.lastIndexOf('/');
            if ((hash == -1 && slash == -1)) {
                // do nothing
            } else if (hash > slash) {
                try {
                    name = (hash > slash) ? name.substring(hash) : name.substring(slash);
                } catch (Exception e) {
                    logger.error("this string heuristic seems not to be good", e);
                }

            }
        }
        return name;
    }

    /**
     * @param uri
     * @return The prefixed URI, or the full uri, if no prefix was found
     */
    public String prefixUri(String uri) {
        String name = uri.replace(base, "");
        for (String s : namespaceToPrefix.getMap().keySet()) {
            name = name.replace(s, namespaceToPrefix.getMap().get(s) + ":");
        }
        return name;
    }


}
