package org.aksw.commons.util.range;

/** Util class to map a global offset to a page id and relative offset within the page - w.r.t. a given page size */
public class PageHelperImpl {

    protected int pageSize;

    public PageHelperImpl(int pageSize) {
        super();
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }
}
