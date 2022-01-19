package org.aksw.commons.beans.datatype;

public interface MapType
    extends DataType
{
    DataType getKeyType();
    DataType getValueType();
}
