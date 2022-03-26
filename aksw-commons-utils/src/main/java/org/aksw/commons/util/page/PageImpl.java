package org.aksw.commons.util.page;

public class PageImpl
    implements Page
{
    protected long pageNumber;
    protected long pageOffset;
    protected boolean isActive;

    public PageImpl() {
    }

    @Override
    public long getPageNumber() { return pageNumber; }
    public void setPageNumber(long pageNumber) { this.pageNumber = pageNumber; }

    @Override
    public long getPageOffset() { return pageOffset; }
    public void setPageOffset(long pageOffset) { this.pageOffset = pageOffset; }

    @Override
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}