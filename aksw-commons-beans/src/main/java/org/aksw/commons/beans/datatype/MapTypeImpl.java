package org.aksw.commons.beans.datatype;

public class MapTypeImpl
    implements MapType
{
    protected DataType keyType;
    protected DataType valueType;

    public MapTypeImpl(DataType keyType, DataType valueType) {
        super();
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public DataType getKeyType() {
        return keyType;
    }

    public DataType getValueType() {
        return valueType;
    }
}
