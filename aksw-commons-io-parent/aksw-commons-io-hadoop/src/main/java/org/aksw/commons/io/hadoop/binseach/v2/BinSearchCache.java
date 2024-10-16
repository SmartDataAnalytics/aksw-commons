package org.aksw.commons.io.hadoop.binseach.v2;

interface BinSearchCache {
    long getDisposition(long position);
    void setDisposition(long from, long to);

    HeaderRecord getHeader(long position);
    void setHeader(HeaderRecord headerRecord);
}
