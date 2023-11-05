package org.aksw.commons.io.buffer.array;

// Unfinished; but could eventually be used to abstract access to arrays, lists, sql result rows and spark rows
public interface RowAccessor<R> {
    byte getByte(R row, int index);
    void setByte(R row, int index, byte value);

    @SuppressWarnings("unchecked")
    default byte getByteRaw(Object row, int index) {
        return getByte((R)row, index);
    }

    @SuppressWarnings("unchecked")
    default void setByteRaw(Object row, int index, byte value) {
        setByte((R)row, index, value);
    }
}
