package org.aksw.commons.util.serializable;

import java.io.Serializable;
import java.util.IntSummaryStatistics;

public class SerializableIntSummaryStatistics
    extends IntSummaryStatistics
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SerializableIntSummaryStatistics() {
        super();
    }

    public SerializableIntSummaryStatistics(long count, int min, int max, long sum) throws IllegalArgumentException {
        super(count, min, max, sum);
    }
}
