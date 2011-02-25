package org.aksw.commons.util.experiments.impl;

import org.aksw.commons.util.experiments.Table;
import org.aksw.commons.util.experiments.TableFormatter;
import org.aksw.commons.util.experiments.TableRowColumn;

import java.text.DecimalFormat;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class GNUPlotColumnFormatter implements TableFormatter {

    DecimalFormat dfGnuPlotDefault = new DecimalFormat("######0.00####");


    boolean addNumbersInFront = true;
    boolean replaceCommaByPoints = true;

    public String format(Table table) {
        String[] rows = new String[table.size() + 1];
        for (int i = 0; i < table.size() + 1; i++) {
            rows[i] = "";
        }

        for (int a = 0; a < table.getTableRowColumns().size(); a++) {
            TableRowColumn trc = table.getTableRowColumns().get(a);
            String header = trc.getExperimentName() + " " + trc.getLabel();
            boolean firstColumn = (a == 0);
            boolean lastColumn = (a + 1 == table.getTableRowColumns().size());
            for (int i = 1; i < table.size() + 1; i++) {

                boolean firstRow = (i == 1);
                rows[0] += (firstRow ? "#" + header + "\t" : "");
                rows[i] += (firstColumn && addNumbersInFront ? i + "\t" : "");
                rows[i] += getEntry(trc, i - 1) + ((lastColumn) ? "" : "\t");

            }
        }
        String ret = "";
        for (int i = 0; i < table.size() + 1; i++) {
            ret += rows[i] + "\n";
        }

        return (replaceCommaByPoints) ? ret.replace(",", ".") : ret;
    }


    private String getEntry(TableRowColumn trc, int i) {
        return dfGnuPlotDefault.format(trc.getFinalizedMonitors()[i].getAvg()) + "";
    }

}
