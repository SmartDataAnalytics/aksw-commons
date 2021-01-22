package org.aksw.commons.util.apache;

import org.aksw.commons.collections.IClosableIterator;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.commons.collections.WindowedSorterIterator;

import java.io.File;
import java.util.*;

public class ApacheLogRangeEntryIterator
	extends SinglePrefetchIterator<ApacheLogEntry>
{
	//private NavigableMap<Date, File> dateToFile = new TreeMap<Date, File>();
	private Iterator<Map.Entry<Date, File>> itFile;
	
	//private ApacheLogEntryIterator itEntry;
	private IClosableIterator<ApacheLogEntry> itEntry;
	
	
	private NavigableMap<Date, ApacheLogEntry> sortedBuffer = new TreeMap<Date, ApacheLogEntry>();
	int maxSortedBufferSize = 1000;
	
	private Date low;
	private Date high;
	private boolean lowInclusive;
	private boolean highInclusive;
	
	private Date sanityCheckMonotonictyDate = null;
	
	public ApacheLogRangeEntryIterator(Iterator<Map.Entry<Date, File>> itFile, Date low, boolean lowInclusive, Date high, boolean highInclusive)
	{
		this.itFile = itFile;
		this.low = low;
		this.high = high;
		this.lowInclusive = lowInclusive;
		this.highInclusive = highInclusive;
	}

	@Override
	protected ApacheLogEntry prefetch() throws Exception
	{
		while(sortedBuffer.size() < maxSortedBufferSize) {
			if(itEntry == null) {
				if(itFile.hasNext()) {
					itEntry = WindowedSorterIterator.wrap(new ApacheLogEntryIterator(ApacheLogDirectory.open(itFile.next().getValue()), true), 1000, new ApacheLogEntryDateComparator());
				} else {
					break;
				}
			}
			
			if(!itEntry.hasNext()) {
				itEntry = null;
				continue;
			}

			ApacheLogEntry entry = itEntry.next();
			
			if(low != null) {
				int d = entry.getDate().compareTo(low);
				if(d < 0 || (d == 0 && !lowInclusive)) {
					continue;
				}
			}

			sortedBuffer.put(entry.getDate(), entry);
		}
			
			
		if(!sortedBuffer.isEmpty()) {
			ApacheLogEntry entry = sortedBuffer.pollFirstEntry().getValue();

			if(sanityCheckMonotonictyDate != null) {
				if(sanityCheckMonotonictyDate.compareTo(entry.getDate()) > 0) {
					throw new RuntimeException("Dates are not monoton");
				}
			}
			sanityCheckMonotonictyDate = entry.getDate();
			
			if(high != null) {
				int d = high.compareTo(entry.getDate());
				if(d < 0 || (d == 0 && !highInclusive)) {
					return finish();
				}				
			}
			

			return entry;
		}
		
		return finish();
	}
	
	@Override
	public void close()
	{
		if(itEntry != null) {
			itEntry.close();
		}
	}
}