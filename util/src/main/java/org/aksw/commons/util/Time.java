package org.aksw.commons.util;

import java.text.DecimalFormat;

public class Time {

	public static DecimalFormat dfGnuPlotDefault = new DecimalFormat("######0.00####");
	public static DecimalFormat dfLatexDefault = new DecimalFormat("####.####");
	public static DecimalFormat dfPercentage = new DecimalFormat("##.##%");

	public static String neededMs(double timeInMs) {
		return "(needed " + Math.round(timeInMs) + " ms.)";
	}

}
