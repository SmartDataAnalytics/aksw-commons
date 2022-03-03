package org.aksw.commons.beans.datatype;

public class SetTypeImpl
    extends CollectionTypeBase
    implements SetType
{
    public SetTypeImpl(DataType collectionType, DataType itemType) {
        super(collectionType, itemType);
    }
}
