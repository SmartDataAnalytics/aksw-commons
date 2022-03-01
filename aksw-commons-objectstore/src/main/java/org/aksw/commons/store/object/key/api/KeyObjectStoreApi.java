package org.aksw.commons.store.object.key.api;

public interface KeyObjectStoreApi {
    ObjectResource get(String... keySegments);
}
