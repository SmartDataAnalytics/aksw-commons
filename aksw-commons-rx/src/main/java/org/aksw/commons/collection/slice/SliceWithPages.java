package org.aksw.commons.collection.slice;

import org.aksw.commons.rx.cache.range.BufferView;
import org.aksw.commons.util.range.PageHelper;
import org.aksw.commons.util.ref.RefFuture;

public interface SliceWithPages<T>
    extends SliceWithAutoSync<T>, PageHelper
{
    // ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> getClaimedPages();
    @Override
    long getPageSize();

    RefFuture<BufferView<T>> getPageForPageId(long pageId);
}
