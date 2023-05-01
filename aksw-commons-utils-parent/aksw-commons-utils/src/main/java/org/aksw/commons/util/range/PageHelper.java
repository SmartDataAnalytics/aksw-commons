package org.aksw.commons.util.range;

/**
 * Interface to ease working with fixed size pages.
 *
 * @author raven
 *
 */
public interface PageHelper {
    long getPageSize();

    default long getPageOffsetForPageId(long pageId) {
        long pageSize = getPageSize();
        return getPageOffsetForPageId(pageId, pageSize);
    }

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

    public static long getPageOffsetForPageId(long pageId, long pageSize) {
        return pageId * pageSize;
    }

    public static long getLastPageId(long size, long pageSize) {
        return size / pageSize;
    }
}
