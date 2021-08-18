package org.aksw.commons.util.range;

public interface PageHelper {
    long getPageSize();

    default long getPageIdForOffset(long offset) {
        long pageSize = getPageSize();
        return getPageIdForOffset(offset, pageSize);
    }

    default long getIndexInPageForOffset(long offset) {
        long pageSize = getPageSize();
        return getIndexInPageForOffset(offset, pageSize);
    }

    public static long getPageIdForOffset(long offset, long pageSize) {
        long result = offset / pageSize;
        return result;
    }

    public static long getIndexInPageForOffset(long offset, long pageSize) {
        return offset % pageSize;
    }

}
