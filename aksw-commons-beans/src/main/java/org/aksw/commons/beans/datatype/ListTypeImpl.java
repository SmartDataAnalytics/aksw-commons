package org.aksw.commons.beans.datatype;

public class ListTypeImpl
    extends CollectionTypeBase
    implements ListType
{
    public ListTypeImpl(DataType collectionType, DataType itemType) {
        super(collectionType, itemType);
    }
}
