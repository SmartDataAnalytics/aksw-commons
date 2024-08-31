package org.aksw.commons.io.binseach;

import java.io.InputStream;
import java.util.stream.Stream;

import org.aksw.commons.io.input.ReadableChannelSupplier;

public class BinarySearchRequestBuilder {
    protected byte[] prefix;
    protected long splitSize;
    protected int splitCount;

    public BinarySearchRequestBuilder setPrefix(byte[] prefix) {
        this.prefix = prefix;
        return this;
    }

    public BinarySearchRequestBuilder setPrefix(String prefix) {
        this.prefix = prefix == null ? null : prefix.getBytes();
        return this;
    }

    public BinarySearchRequestBuilder setSplitSize(long splitSize) {
        this.splitSize = splitSize;
        this.splitCount = -1;
        return this;
    }

    public BinarySearchRequestBuilder setSplitCount(int splitCount) {
        this.splitCount = splitCount;
        this.splitSize = -1;
        return this;
    }

    public InputStream find() {
        return null;
    }

    public Stream<ReadableChannelSupplier<byte[]>> findParallel() {
        return null;
    }
}
