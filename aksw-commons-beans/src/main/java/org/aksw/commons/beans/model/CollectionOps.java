package org.aksw.commons.beans.model;

import java.util.Iterator;

/** TODO This class can be consolidated with org.aksw.commons.util.collection.CollectionOps */
public interface CollectionOps {
    Iterator<?> getItems(Object entity);
    void setItems(Object entity, Iterator<?> items);
}
