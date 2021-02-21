package org.aksw.commons.io.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

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

}
