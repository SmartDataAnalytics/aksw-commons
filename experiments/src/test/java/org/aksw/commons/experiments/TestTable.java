package org.aksw.commons.experiments;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.experiments.impl.GNUPlotColumnFormatter;
import org.aksw.commons.experiments.impl.GNUPlotRowFormatter;
import org.aksw.commons.experiments.impl.LatexColumnFormatter;
import org.aksw.commons.experiments.impl.LatexRowFormatter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class TestTable {

    List<TableRowColumn> trc = new ArrayList<TableRowColumn>();

    @Before
    public void init() {

        int size = 5;
        trc.add(new TableRowColumn("Precision", "TestExp", Static.Units.DOUBLE, initializeMonitorArray(size)));
        trc.add(new TableRowColumn("Recall", "TestExp", Static.Units.PERCENTAGE, initializeMonitorArray(size)));
        trc.add(new TableRowColumn("Time", "TestExp", Static.Units.MILLISECONDS, initializeMonitorArray(size)));
    }

    @Test
    public void t(){
        Table t = new Table();
        t.addTableRowColumns(trc);

        System.out.println(new GNUPlotColumnFormatter().format(t));
        System.out.println(new GNUPlotRowFormatter().format(t));
        System.out.println(new LatexColumnFormatter().format(t));
        System.out.println(new LatexRowFormatter().format(t));

    }


    public static Monitor[] initializeMonitorArray(int size) {
        Monitor[] m = new Monitor[size];
        for (int x = 0; x < m.length; x++) {
            m[x] = MonitorFactory.getMonitor();
            Random r = new Random();
            for (int i = 0; i < 10; i++) {
                m[x].add(r.nextDouble());
            }
        }
        return m;
    }


}
