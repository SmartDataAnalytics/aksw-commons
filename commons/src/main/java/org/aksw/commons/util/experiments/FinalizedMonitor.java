package org.aksw.commons.util.experiments;

import com.jamonapi.MonKey;
import com.jamonapi.Monitor;

import java.io.Serializable;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */

/**
 * This a class to make a Jamon monitor persistent
 *
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class FinalizedMonitor implements Serializable {

    final String header;
    final String units;

    final double avg;
    final double hits;
    final double stdDev;
    final double lastValue;
    final double max;
    final double min;
    final double total;


    public FinalizedMonitor(Monitor m) {
        header = (String) m.getMonKey().getValue(MonKey.LABEL_HEADER);
        units = (String) m.getMonKey().getValue(MonKey.UNITS_HEADER);
        avg = m.getAvg();
        hits = m.getHits();
        stdDev = m.getStdDev();
        lastValue = m.getLastValue();
        max = m.getMax();
        min = m.getMin();
        total = m.getTotal();


    }

    public String getHeader() {
        return header;
    }

    public String getUnits() {
        return units;
    }


    public double getAvg() {
        return avg;
    }


    public double getHits() {
        return hits;
    }


    public double getStdDev() {
        return stdDev;
    }

    public double getLastValue() {
        return lastValue;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getTotal() {
        return total;
    }


}
