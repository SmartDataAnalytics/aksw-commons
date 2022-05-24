package org.aksw.commons.io.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class UriUtils {

    public static URL toURL(URI uri) {
        URL result;
        try {
            result = uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static URI newURI(String uri) {
        URI result;
        try {
            result = new URI(uri);
        } catch (URISyntaxException e) {
            result = null;
        }
        return result;
    }

    public static Optional<URI> tryNewURI(String uri) {
        Optional<URI> result = Optional.ofNullable(newURI(uri));
        return result;
    }



    /**
     * Only retains first value
     * @return
     */
//    public static Map<String, String> createMapFromUriQueryString(URI uri) {
//        return createMapFromUriQueryString(uri);
//    }

    /**
     * Only retains first value
     * @return
     */
    public static Map<String, String> parseQueryStringAsMap(String queryString) {
        Multimap<String, String> multimap = parseQueryString(queryString);
        return toMap(multimap, LinkedHashMap::new);
    }

    public static <K, V> Map<K, V> toMap(Multimap<K, V> mm, Supplier<? extends Map<K, V>> mapSupplier) {
        return mm.entries().stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, mapSupplier));
    }


    public static Multimap<String, String> parseQueryString(String queryString) {
        try {
            return parseQueryStringEx(queryString);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Multimap<String, String> parseQueryStringEx(String queryString)
            throws UnsupportedEncodingException
    {
        Multimap<String, String> result = ArrayListMultimap.create();

        if(queryString == null) {
            return result;
        }

        for (String param : queryString.split("&")) {
            String pair[] = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], "UTF-8");
            }
            result.put(new String(key), new String(value));
        }

        return result;
    }
}
