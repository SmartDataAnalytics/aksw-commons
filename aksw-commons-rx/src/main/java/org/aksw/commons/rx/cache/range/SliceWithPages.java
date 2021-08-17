package org.aksw.commons.rx.cache.range;

import org.aksw.commons.util.range.BufferWithGeneration;
import org.aksw.commons.util.range.PageHelper;
import org.aksw.commons.util.ref.RefFuture;

public interface SliceWithPages<T>
    extends Slice<T>, PageHelper
{
    // ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> getClaimedPages();
    int getPageSize();

    RefFuture<BufferWithGeneration<T>> getPageForPageId(long pageId);
}
