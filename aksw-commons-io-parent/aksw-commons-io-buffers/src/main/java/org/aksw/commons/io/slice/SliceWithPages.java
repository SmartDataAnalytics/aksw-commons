package org.aksw.commons.io.slice;

import org.aksw.commons.util.range.PageHelper;
import org.aksw.commons.util.ref.RefFuture;

public interface SliceWithPages<T>
    extends Slice<T>, PageHelper
{
    @Override
    long getPageSize();

    default SliceAccessor<T> newSliceAccessor() {
        return new SliceAccessorImpl<>(this);
    }

    RefFuture<BufferView<T>> getPageForPageId(long pageId);
}
