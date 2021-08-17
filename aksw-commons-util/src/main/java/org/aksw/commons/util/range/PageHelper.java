package org.aksw.commons.util.range;

public interface PageHelper {
    int getPageSize();

    default long getPageIdForOffset(long offset) {
        int pageSize = getPageSize();
        return getPageIdForOffset(offset, pageSize);
    }

    default int getIndexInPageForOffset(long offset) {
        int pageSize = getPageSize();
        return getIndexInPageForOffset(offset, pageSize);
    }

    public static long getPageIdForOffset(long offset, int pageSize) {
        long result = offset / pageSize;
        return result;
    }

    public static int getIndexInPageForOffset(long offset, int pageSize) {
        return (int)(offset % (long)pageSize);
    }

}
