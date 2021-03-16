package org.aksw.commons.io.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public class UrlUtils {
    public static URL newURL(String uri) {
        // There was some reason why to go from String to URL via URI... but i forgot...
    	// Probably it was due to issues with file:// urls 
        URI tmp = UriUtils.newURI(uri);
        URL result;
        try {
          result = tmp.toURL();
      } catch (MalformedURLException e) {
          throw new RuntimeException(e);
      }
        return result;
    }

    public static Optional<URL> tryNewURL(String uri) {
        Optional<URL> result = Optional.ofNullable(newURL(uri));
        return result;
    }

}
