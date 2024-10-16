package org.aksw.commons.io.hadoop.binseach.v2;

class BinarySearchCacheNoop
    implements BinSearchCache
{
    @Override
    public long getDisposition(long position) { return -1; }

    @Override
    public void setDisposition(long from, long to) {}

    @Override
    public HeaderRecord getHeader(long position) { return null; }

    @Override
    public void setHeader(HeaderRecord headerRecord) { }
}
