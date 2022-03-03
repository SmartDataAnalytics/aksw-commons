package org.aksw.commons.util.serializable;

import java.io.Serializable;
import java.util.DoubleSummaryStatistics;

public class SerializableDoubleSummaryStatistics
    extends DoubleSummaryStatistics
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SerializableDoubleSummaryStatistics() {
        super();
    }

    public SerializableDoubleSummaryStatistics(long count, double min, double max, double sum) throws IllegalArgumentException {
        super(count, min, max, sum);
    }
}
