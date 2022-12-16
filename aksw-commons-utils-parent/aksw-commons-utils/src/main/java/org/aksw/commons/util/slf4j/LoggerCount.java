package org.aksw.commons.util.slf4j;

import org.slf4j.Logger;
import org.slf4j.ext.LoggerWrapper;

/**
 * Wraps a logger and counts errors.
 *
 * Note: some ppl used appenders for this, but I do not think this is a good idea.
 * If appenders are not properly initialized, it won't work.
 *
 * TODO There just has to be some class which already does the counting... but I could'nt find it yet
 * TODO The implementation is not complete, i.e. it only warps a small a subset of the method calls
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class LoggerCount
	extends LoggerWrapper
{
	public LoggerCount(Logger logger) {
		super(logger, "Counting " + logger.getName());
	}

	public LoggerCount(Logger logger, String fqcn) {
		super(logger, fqcn);
	}

	private int errorCount = 0;
	private int warningCount = 0;

	@Override
	public void error(String message) {
		System.err.println(message);
		++errorCount;
	}

	//@Override
	public void warn(String message) {
		System.err.println(message);
		++warningCount;
	}


	public int getErrorCount() {
		return this.errorCount;
	}

	public int getWarningCount() {
		return this.warningCount;
	}
}