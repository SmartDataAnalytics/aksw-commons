package org.aksw.commons.util.experiments.impl;

import org.aksw.commons.util.experiments.FinalizedMonitor;
import org.aksw.commons.util.experiments.Table;
import org.aksw.commons.util.experiments.TableFormatter;
import org.aksw.commons.util.experiments.TableRowColumn;

import javax.management.monitor.Monitor;
import java.util.List;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class LatexColumnFormatter implements TableFormatter {

    boolean replaceCommaByPoints = true;
    boolean addNumbersInFront = true;
    public static String latexSep = "\t&\t";
        public static String latexEnd = "\\\\";
    
	

	public String format( Table table) {
        int length = table.size();
        List<TableRowColumn> tableRowColumns = table.getTableRowColumns();

		String[] rows = new String[length+1];
		for (int i = 0; i < length+1; i++) {
			rows[i]="";
		}

		for (int a = 0; a < tableRowColumns.size(); a++) {
			TableRowColumn trc = tableRowColumns.get(a);
			String header = trc.getExperimentName()+" "+trc.getLabel();
			boolean firstColumn = (a==0);
			boolean lastColumn = (a + 1 == tableRowColumns.size());
			for (int i = 1; i < length+1; i++) {

				boolean firstRow = (i==1);
						rows[0] += ((firstColumn&&firstRow&&addNumbersInFront)?latexSep:"");
						rows[0] += (firstRow?header+latexSep:"");
						rows[i] += ((firstColumn&&addNumbersInFront)?i+latexSep:"");
						rows[i] += getLatexEntry(trc.getFinalizedMonitors()[i-1])+ ((lastColumn) ? latexSep : latexSep);

			}
		}
		String ret = "";
		for (int i = 0; i < length+1; i++) {
			ret += rows[i]+"\n";
		}

		return (replaceCommaByPoints)?ret.replace(",","."):ret;
	}


    public String getLatexEntry(FinalizedMonitor m) {
		return m.getAvg() + " ";
	}

/*
    public String getLatexEntry(int i) {
		return latexFormat(monitors[i], getValue(i)) + " "+getLatexStdDev(i) ;
	}

    private String getLatexStdDev(int i){
		String tex = "(\\pm"+latexFormat(monitors[i], monitors[i].getStdDev()) + ") ";
		if(useStdDev){
			return tex;
		}

		if(useStdDevWithPercentageUnit && monitors[i].getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return tex;
		}

		return "";

	}

    private String latexFormat(FinalizedMonitor monitors, double value){
		if(monitors.getUnits().equals(JamonMonitorLogger.PERCENTAGE)){
			return dfPercentage.format(value).replace("%", "\\%").replace("_", "\\_");
		}else{
			return dfLatexDefault.format(value);
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
