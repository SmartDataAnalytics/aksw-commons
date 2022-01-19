package org.aksw.commons.beans.datatype;

public class CollectionTypeBase
    implements CollectionType
{
    protected DataType collectionType;
    protected DataType itemType;

    public CollectionTypeBase(DataType collectionType, DataType itemType) {
        super();
        this.collectionType = collectionType;
        this.itemType = itemType;
    }

    @Override
    public DataType getCollectionType() {
        return collectionType;
    }

    @Override
    public DataType getItemType() {
        return itemType;
    }
}
