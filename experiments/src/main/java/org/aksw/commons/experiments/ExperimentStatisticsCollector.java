package org.aksw.commons.experiments;

import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class ExperimentStatisticsCollector {

    private final String experimentName;
    private final int nrOfIterations;
    private final Map<String, Static.Units> map = new HashMap<String, Static.Units>();

    /**
     * @param experimentName such as "now_with_more_sugar"
     * @param nrOfIterations
     */
    public ExperimentStatisticsCollector(String experimentName, int nrOfIterations) {
        this.experimentName = experimentName;
        this.nrOfIterations = nrOfIterations;
    }

    /**
     * @param label     such as precision
     * @param iteration
     * @param unit
     * @return
     */
    public Monitor getMonitor(String label, int iteration, Static.Units unit) {
        map.put(label, unit);
        return MonitorFactory.getMonitor(makeMonKey(label, iteration, unit));
    }

    public TableRowColumn getTableColumnRow(String label, Static.Units unit) {
        Monitor[] m = new Monitor[nrOfIterations];
        for (int i = 0; i < nrOfIterations; i++) {
            m[i] = getMonitor(label, i, unit);
        }
        TableRowColumn trc =  new TableRowColumn(experimentName, label, unit, m);
        trc.finalize();
        return trc;
    }

    public Table getTable() {
        Table t = new Table();
        for (String label : map.keySet()) {
            Static.Units u = map.get(label);
            t.addTableRowColumn(getTableColumnRow(label, u));
        }
        return t;
    }

    private String makeMonitorLabel(String label, int i) {
        return makeMonitorLabel(label) + "_" + i;
    }

    private String makeMonitorLabel(String label) {
        return experimentName + "_" + label;
    }


    private MonKeyImp makeMonKey(String label, Static.Units unit) {
        return new MonKeyImp(makeMonitorLabel(label), Static.getUnitString(unit));
    }

    private MonKeyImp makeMonKey(String label, int i, Static.Units unit) {
        return new MonKeyImp(makeMonitorLabel(label, i), Static.getUnitString(unit));
    }


}
