package org.aksw.commons.util.apache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.PatternFilenameFilter;

public class ApacheLogDirectory
{
	private static final Logger logger = LoggerFactory.getLogger(ApacheLogDirectory.class);
	
	private NavigableMap<Date, File> dateToFile = new TreeMap<Date, File>();

	public static InputStream open(File file)
		throws IOException
	{
		InputStream in = new FileInputStream(file);
		if(file.getName().endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		
		return in;
	}
	
	public ApacheLogDirectory(File dir, Pattern pattern) throws IOException
	{
		if(!dir.isDirectory()) {
			throw new IllegalArgumentException("Argument must be a directory, got: " + dir);
		}
		
		// Read the date from the first line of each file, and sort the files
		// accordingly
		for(File file : dir.listFiles(new PatternFilenameFilter(pattern))) {
			ApacheLogEntryIterator it = new ApacheLogEntryIterator(open(file), true);
			
			while(it.hasNext()) {
				Date date = it.next().getDate();
				//logger.debug("Found log file " + file.getAbsolutePath() + " with date " + date);
				dateToFile.put(date, file);
				it.close();
				break;
			}
		}
		logger.debug("Found log files: " + dateToFile);
	}
	
	/**
	 * 
	 * lowInclusive and highInclusive are ignored if the respective bound in null (unbounded).
	 * 
	 * @param low
	 * @param lowInclusive
	 * @param high
	 * @param highInclusive
	 * @return
	 */
	public ApacheLogRangeEntryIterator getIterator(Date low, Date high, boolean lowInclusive, boolean highInclusive)
	{
		NavigableMap<Date, File> subMap = dateToFile; 
		
		Date adjustedLow = low == null ? null : dateToFile.floorKey(low);
		Date adjustedHigh = high == null ? null : dateToFile.ceilingKey(high);
		
		// lower bound
		if(adjustedLow != null) {
			subMap = subMap.tailMap(adjustedLow, true);
		}
		
		// upperbound
		if(adjustedHigh != null) {
			subMap = subMap.headMap(adjustedHigh, true);
		}

		logger.debug("Adjust: Creating an iterator from " + adjustedLow + " until " + adjustedHigh + "; spanning " + subMap.size() + " files.");
		logger.debug("Creating an iterator from " + low + " until " + high + "; spanning " + subMap.size() + " files.");
		
		return new ApacheLogRangeEntryIterator(subMap.entrySet().iterator(), low, lowInclusive, high, highInclusive);
	}
}
