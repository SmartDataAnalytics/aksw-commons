package org.aksw.commons.io.slice;

import java.util.List;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

public class SliceMetaDataWithPagesImpl
    extends SliceMetaDataImpl
    implements SliceMetaDataWithPages
{
    private static final long serialVersionUID = 1L;

    protected int pageSize;

    public SliceMetaDataWithPagesImpl() {
        this(1024 * 64);
    }

    public SliceMetaDataWithPagesImpl(int pageSize, RangeSet<Long> loadedRanges,
            RangeMap<Long, List<Throwable>> failedRanges, long minimumKnownSize, long maximumKnownSize) {
        super(loadedRanges, failedRanges, minimumKnownSize, maximumKnownSize);

        this.pageSize = pageSize;
    }

    public SliceMetaDataWithPagesImpl(int pageSize) {
        super();
        this.pageSize = pageSize;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + pageSize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SliceMetaDataWithPagesImpl other = (SliceMetaDataWithPagesImpl) obj;
        if (pageSize != other.pageSize)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SliceMetaDataWithPagesImpl [pageSize=" + pageSize + ", toString()=" + super.toString() + "]";
    }
}
