package org.aksw.commons.experiments.impl;


import org.aksw.commons.experiments.FinalizedMonitor;
import org.aksw.commons.experiments.Table;
import org.aksw.commons.experiments.TableFormatter;
import org.aksw.commons.experiments.TableRowColumn;

import java.util.List;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class LatexRowFormatter implements TableFormatter {

      public static String latexSep = "\t&\t";
        public static String latexEnd = "\\\\";

    boolean replaceCommaByPoints = true;

    public String format(Table table) {
        List<TableRowColumn> tableRowColumns = table.getTableRowColumns();

        String ret = "";
        for (TableRowColumn trc : tableRowColumns) {

            ret += toRow(trc) + "\n";

        }
        return (replaceCommaByPoints) ? ret.replace(",", ".") : ret;
    }


    private String toRow(TableRowColumn trc) {
        FinalizedMonitor[] monitors = trc.getFinalizedMonitors();
        String ret = trc.getExperimentName() + " " + trc.getLabel();
        for (int i = 0; i < monitors.length; i++) {
            boolean last = (i + 1 == monitors.length);
            ret += latexSep + getLatexEntry(monitors[i]) + (last ? latexEnd : "");
        }
        return ret;

    }


     public String getLatexEntry(FinalizedMonitor m ) {
        return m.getAvg()+"";
    }

}

    /*
    public String getLatexEntry(int i) {
        return latexFormat(monitors[i], getValue(i)) + " " + getLatexStdDev(i);
    }

    private String getLatexStdDev(int i) {
        String tex = "(\\pm" + latexFormat(monitors[i], monitors[i].getStdDev()) + ") ";
        if (useStdDev) {
            return tex;
        }

        if (useStdDevWithPercentageUnit && monitors[i].getUnits().equals(JamonMonitorLogger.PERCENTAGE)) {
            return tex;
        }

        return "";

    }



    private double getValue(int i) {
//		return monitors[i].getAvg();
        switch (display) {
            case AVG:
                return monitors[i].getAvg();
            case HITS:
                return monitors[i].getHits();
            case TOTAL:
                return monitors[i].getTotal();
        }
        return monitors[i].getAvg();
    }

    private String latexFormat(FinalizedMonitor monitors, double value) {
        if (monitors.getUnits().equals(JamonMonitorLogger.PERCENTAGE)) {
            return dfPercentage.format(value).replace("%", "\\%").replace("_", "\\_");
        } else {
            return dfLatexDefault.format(value);
        }
    }
}
*/