package org.aksw.commons.util.page;

public interface Page {
    public long getPageNumber();
    public long getPageOffset();
    public boolean isActive();
}
