package org.aksw.commons.experiments;

import com.jamonapi.Monitor;

import java.io.Serializable;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class TableRowColumn implements Serializable {

    private final String label;
    private final String experimentName;
    private final Static.Units unit;
    private transient final Monitor[] monitors;
    private FinalizedMonitor[] finalizedMonitors = null;

    /**
     * @param monitors       the JamonMonitors
     * @param experimentName The name of the experiment, could be e.g. a parameter ("special noise ratio" or "baseline")
     * @param label          the label for this specific vector of values such as Precision, Recall ....
     */
    public TableRowColumn(String experimentName, String label,  Static.Units unit, Monitor[] monitors) {
        this.label = label;
        this.experimentName = experimentName;
        this.unit = unit;
        this.monitors = monitors;
    }


    public int size() {
        return monitors.length;
    }

    public String getLabel() {
        return label;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public FinalizedMonitor[] getFinalizedMonitors() {
        if (finalizedMonitors == null) {
            finalize();
        }
        return finalizedMonitors;
    }

    public Monitor getJamMonitors(int i) {
        return monitors[i];
    }

    public void finalize() {
        this.finalizedMonitors = new FinalizedMonitor[monitors.length];
        for (int i = 0; i < monitors.length; i++) {
            this.finalizedMonitors[i] = new FinalizedMonitor(monitors[i]);
        }
    }

    /*
   * stupid things below
   *
   * */

/*
	public enum Display {
		AVG, HITS, TOTAL
	}

	public static boolean useStdDevWithPercentageUnit = true;

	public static String latexSep = "\t&\t";
	public static String latexEnd = "\\\\";



//	final Monitor[] monitors;

	boolean useStdDev = false;

	Display display = Display.AVG;

	DecimalFormat dfGnuPlotDefault = new DecimalFormat("######0.00####");

//	DecimalFormat dfStdDevLatex = new DecimalFormat("##.##%");
	DecimalFormat dfLatexDefault = new DecimalFormat("####.####");
	DecimalFormat dfPercentage = new DecimalFormat("##.##%");

	// public TableRowColumn(Monitor[] monitors){
	// this.monitors = monitors;
	// }





	public void deleteAll(){
		for (int i = 0; i < monitors.length; i++) {
//			MonitorFactory.remove(monitors[i].getMonKey());
		}
	}


	private double getValue(int i){
//		return monitors[i].getAvg();
		switch(display){
			case AVG: return monitors[i].getAvg();
			case HITS: return monitors[i].getHits();
			case TOTAL: return monitors[i].getTotal();
		}
		return monitors[i].getAvg();
	}
*/

}

