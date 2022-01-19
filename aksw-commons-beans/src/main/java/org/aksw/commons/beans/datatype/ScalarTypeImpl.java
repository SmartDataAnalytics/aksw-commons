package org.aksw.commons.beans.datatype;

public class ScalarTypeImpl
    implements ScalarType
{
    protected Class<?> javaClass;

    public ScalarTypeImpl(Class<?> javaClass) {
        super();
        this.javaClass = javaClass;
    }

    @Override
    public Class<?> getJavaClass() {
        return javaClass;
    }

}
