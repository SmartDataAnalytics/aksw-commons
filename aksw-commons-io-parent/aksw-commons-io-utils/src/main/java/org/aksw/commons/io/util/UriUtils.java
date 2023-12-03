package org.aksw.commons.io.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.LinkedHashMultimap;
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

    public static String encodeUtf8(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String decodeUft8(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
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
        Multimap<String, String> multimap = parseQueryStringAsMultimap(queryString);
        return toMap(multimap, LinkedHashMap::new);
    }

    public static <K, V> Map<K, V> toMap(Multimap<K, V> mm, Supplier<? extends Map<K, V>> mapSupplier) {
        return mm.entries().stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, mapSupplier));
    }

    public static Multimap<String, String> parseQueryStringAsMultimap(String queryString) {
        Multimap<String, String> result = LinkedHashMultimap.create();
        parseQueryStringAsEntries(queryString, result::put);
        return result;
    }

    public static List<Entry<String, String>> parseQueryStringAsList(String queryString) {
        List<Entry<String, String>> result = new ArrayList<>();
        parseQueryStringAsEntries(queryString, (k, v) -> result.add(new SimpleImmutableEntry<>(k, v)));
        return result;
    }

    public static void parseQueryStringAsEntries(String queryString, BiConsumer<String, String> sink) {
        if (queryString != null && !queryString.isBlank()) {
            for (String param : queryString.split("&")) {
                String pair[] = param.split("=", 2);
                String key = decodeUft8(pair[0]);
                String value = null;
                if (pair.length > 1) {
                    value = decodeUft8(pair[1]);
                }
                sink.accept(key, value);
            }
        }
    }

    public static String toQueryString(Multimap<String, String> args) {
        return toQueryString(args.entries());
    }

    public static String toQueryString(Collection<Entry<String, String>> entries) {
        String tmp = entries.stream().map(e -> {
            String k = e.getKey();
            String v = e.getValue();
            String encodedK = encodeUtf8(k);
            String r = v == null
                    ? encodedK
                    : encodedK + "=" + encodeUtf8(v);
            return r;
        }).collect(Collectors.joining("&"));
        String result = !tmp.isEmpty() ? tmp : null;
        return result;
    }

    /** Returns a new URI with its query string replaced directly with the given argument */
    public static URI replaceQueryString(URI uri, String newQueryString) throws URISyntaxException {
        return new URI(uri.getScheme(),
                       uri.getAuthority(),
                       uri.getPath(),
                       newQueryString,
                       uri.getFragment());
    }
}
