package org.aksw.commons.model.csvw.domain.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    // private static final Logger logger = LoggerFactory.getLogger(GsonUtils.class);

    // Workaround for spark's old guava version which may not support setLenient
    public static Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        try {
            builder.setLenient();
        } catch(NoSuchMethodError e) {
            //logger.warn("Gson.setLenient not available");
        }
        Gson result = builder.create();
        return result;
    }
}
