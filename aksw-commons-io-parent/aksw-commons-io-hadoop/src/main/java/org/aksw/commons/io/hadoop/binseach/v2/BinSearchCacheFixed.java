package org.aksw.commons.io.hadoop.binseach.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

class BinSearchCacheFixed
    implements BinSearchCache
{
    /** Map from absolute position to record offset*/
    protected NavigableMap<Long, Long> fixedDispositions;
    protected Map<Long, HeaderRecord> fixedHeaders;

    public BinSearchCacheFixed() {
        super();
        this.fixedDispositions = new TreeMap<>();
        this.fixedHeaders = new HashMap<>();
    }

    @Override
    public long getDisposition(long position) {
        long result = -1;
        if (fixedHeaders.containsKey(position)) {
            result = position;
        } else {
            Entry<Long, Long> e = fixedDispositions.floorEntry(position);
            if (e != null) {
                // long from = e.getKey();
                long to = e.getValue();
                if (position <= to) {
                    result = to;
                }
            }
        }
        return result;
    }

    @Override
    public void setDisposition(long from, long to) {
        Entry<Long, Long> e = fixedDispositions.floorEntry(from);
        if (e != null) {
            long cachedFrom = e.getKey();
            long cachedTo = e.getValue();

            // issues to check for:
            // TODO cached range overlaps with starting point (cachedTo > from)
            if (cachedTo > to) {
                // new:       [        ]
                // cached:    [    ]
                throw new IllegalStateException(String.format("The upper endoint overlaps with an existing entry: [%d, %d] -> [%d, %d]", from, to, cachedFrom, cachedTo));
            } else if (cachedTo == to) {
                // Update an existing entry with a lower boundary
                if (from < cachedFrom) {
                    fixedDispositions.remove(cachedFrom);
                    fixedDispositions.put(from, to);
                }
            } else { // to < cachedTo
                // Sanity check: New's lower endpoint must not overlap
                // new:          [   ]
                // existing: [    ]
                if (from <= cachedTo) {
                    throw new IllegalStateException(String.format("Overlap with an existing entry: [%d, %d] -> [%d, %d]", from, to, cachedFrom, cachedTo));
                }
                fixedDispositions.put(from, to);
            }
        } else {
            fixedDispositions.put(from, to);
        }
    }

    @Override
    public HeaderRecord getHeader(long position) {
        HeaderRecord result = fixedHeaders.get(position);
        return result;
    }

    @Override
    public void setHeader(HeaderRecord headerRecord) {
        // HeaderRecord headerRecord = new HeaderRecord(position, disposition, header, isDataConsumed);
        fixedHeaders.put(headerRecord.position(), headerRecord);
    }
}
