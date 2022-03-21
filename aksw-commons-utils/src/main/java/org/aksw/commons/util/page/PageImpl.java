package org.aksw.commons.util.page;

public class PageImpl
    implements Page
{
    protected long pageNumber;
    protected long pageOffset;
    protected boolean isActive;

    public long getPageNumber() { return pageNumber; }
    public void setPageNumber(long pageNumber) { this.pageNumber = pageNumber; }
    public long getPageOffset() { return pageOffset; }
    public void setPageOffset(long pageOffset) { this.pageOffset = pageOffset; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}