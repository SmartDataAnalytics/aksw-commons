package org.aksw.commons.util.healthcheck;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HealthcheckUtils {

    public static URL createUrl(String str) {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }


    public static void checkUrl(URL url) throws Exception{
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();
            if(code != 200) {
                throw new RuntimeException("Received HTTP status code " + code + " != 200: " + url.toString());
            }
        } finally {
            connection.disconnect();
        }
    }
}
