package org.aksw.commons.util.serializable;

import java.io.Serializable;
import java.util.LongSummaryStatistics;

public class SerializableLongSummaryStatistics
    extends LongSummaryStatistics
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SerializableLongSummaryStatistics() {
        super();
    }

    public SerializableLongSummaryStatistics(long count, long min, long max, long sum) throws IllegalArgumentException {
        super(count, min, max, sum);
    }
}
