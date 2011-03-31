package org.aksw.commons.util;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Note this class is supposed to use certain terminology:
 * uri - the full uri http://dbpedia.org/resource/London
 * prefix - the shortcut canonical to prefix.cc
 * namespace - the "common part" of uris, e.g.  http://dbpedia.org/resource/
 * prefixedUri - namespace replaced with prefix: dbpedia:London
 * NOTE: to use a base prefix add an empty prefix to the map: ""->"http://dbpedia.org/resource/"
 * NOTE: Currently no order is used, namespaces could include other namespaces
 *
 * Created by Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 * A class for handling prefixes
 * Date: 28.01.11
 */
public class Prefixer {
    private Logger logger = LoggerFactory.getLogger(Prefixer.class)     ;
    final private BidiMap<String, String> prefixToNamespace = new DualHashBidiMap<String, String>();
    final private BidiMap<String, String> namespaceToPrefix = prefixToNamespace.inverseBidiMap();


    public Prefixer() {}

    public Prefixer(Map<String, String> prefixToNamespace,   boolean appendPopularPrefixes) {
        this();
        if (appendPopularPrefixes) {
            this.prefixToNamespace.putAll(theMostPopulars());
        }
        this.prefixToNamespace.putAll(prefixToNamespace);
    }

    /**
     * resets everything
     * @param prefixToNamespace
     */
    public void setPrefixToNamespace(Map<String, String> prefixToNamespace) {
        this.prefixToNamespace.clear();
        this.prefixToNamespace.putAll(prefixToNamespace);
    }

    public Map<String, String> theMostPopulars() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.put("owl", "http://www.w3.org/2002/07/owl#");

        m.put("dbpedia", "http://dbpedia.org/resource/");
        m.put("dbo", "http://dbpedia.org/ontology/");
        m.put("dbp", "http://dbpedia.org/property/");

        m.put("foaf", "http://xmlns.com/foaf/0.1/");
        m.put("skos", "http://www.w3.org/2004/02/skos/core#");
        m.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.put("dc", "http://purl.org/dc/elements/1.1/");
        m.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");

        return m;
    }

    /**
     * NOTE base recognition is not implemented
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

    /**
     * http://dbpedia.org/resource/J%C3%BCrgen_Prochnow
     * will be rendered as:
     * Jürgen Prochnow by the following operations:
     * - strip namespace
     * - urldecode
     * - replace _ by whitespace
     * @param uri
     * @return
     */
    public String nicen(String uri) {
        String name = stripNamespace(uri, true);
        try {
            name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }

        name = name.replaceAll("_", " ");
        return name;
    }

    /**
     * this function calls stripNamespace(uri, true);
     * @param uri
     * @return
     */
    public String stripNamespace(String uri){
        return stripNamespace(uri, true);
    }

    /**
     * strips the namespace, either by base or known prefix or a heuristic
     * @param uri e.g. http://dbpedia.org/resource/London
     * @param useHeuristic if true
     * @return  The stripped uri, i.e.   London
     */
    public String stripNamespace(String uri, boolean useHeuristic) {
        for (String namespace: namespaceToPrefix.keySet()) {
            if(uri.startsWith(namespace)){
                return uri.replace(namespace, "");
            }
        }
        return (useHeuristic)?stripNamespaceHeuristic(uri):uri;
    }

    /**
     * currently implemented to cut after the last # or if not available after the last /
     * @param uri e.g. http://dbpedia.org/resource/
     * @return e.g. London
     */
    public static String stripNamespaceHeuristic(String uri){
            String[] separators = new String[]{"#","/"};
            for(String sep : separators){
                int pos = uri.lastIndexOf('#');
                if(pos>0){
                      return uri.substring(pos);
                }
            }
            return uri;
    }

    /**
     * http://dbpedia.org/resource/London -> dbpedia:London
     * @param uri
     * @return The prefixed URI, or the full uri, if no prefix was found
     */
    public String prefixUri(String uri) {
        for (String namespace : namespaceToPrefix.keySet()) {
            if (uri.startsWith(namespace)){
                return uri.replace(namespace, namespaceToPrefix.get(namespace) + ":");
            }
        }
        return uri;
    }


}
