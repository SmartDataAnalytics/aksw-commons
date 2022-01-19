package org.aksw.commons.beans.datatype;

public interface CollectionType
    extends DataType
{
    DataType getCollectionType();
    DataType getItemType();
}
