package org.aksw.commons.util.experiments.impl;

import org.aksw.commons.util.experiments.Table;
import org.aksw.commons.util.experiments.TableFormatter;
import org.aksw.commons.util.experiments.TableRowColumn;

import java.text.DecimalFormat;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class GNUPlotRowFormatter implements TableFormatter {

    DecimalFormat dfGnuPlotDefault = new DecimalFormat("######0.00####");
    boolean replaceCommaByPoints = true;

    public String format(Table table) {
        String ret = "";
        for (TableRowColumn trc : table.getTableRowColumns()) {
            ret += toRow(trc) + "\n";
        }
        return (replaceCommaByPoints) ? ret.replace(",", ".") : ret;
    }

    private String toRow(TableRowColumn trc) {
        String ret = trc.getExperimentName() + " " + trc.getLabel();
        for (int i = 0; i < trc.getFinalizedMonitors().length; i++) {
            boolean last = (i + 1 == trc.getFinalizedMonitors().length);
            ret += "\t" + dfGnuPlotDefault.format(trc.getFinalizedMonitors()[i].getAvg());
        }
        return ret;

    }
}
