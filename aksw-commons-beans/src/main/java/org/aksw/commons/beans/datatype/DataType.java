package org.aksw.commons.beans.datatype;

import java.awt.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * An API over Java types to ease access to item types of collection types and
 * key/value types of maps.
 *
 * @author raven
 *
 */
public interface DataType {
    default boolean isScalarType() {
        return this instanceof ScalarType;
    }

    default ScalarType asScalarType() {
        return (ScalarType)this;
    }


    default boolean isListType() {
        return this instanceof ListType;
    }

    default ListType asListType() {
        return (ListType)this;
    }


    default boolean isSetType() {
        return this instanceof SetType;
    }

    default SetType asSetType() {
        return (SetType)this;
    }


    default boolean isMapType() {
        return this instanceof MapType;
    }

    default MapType asMapType() {
        return (MapType)this;
    }
}
