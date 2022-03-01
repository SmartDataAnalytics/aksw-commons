package org.aksw.commons.io.slice;

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
