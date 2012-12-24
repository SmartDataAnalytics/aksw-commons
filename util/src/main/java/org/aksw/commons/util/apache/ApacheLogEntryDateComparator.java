package org.aksw.commons.util.apache;

import java.util.Comparator;

public class ApacheLogEntryDateComparator
	implements Comparator<ApacheLogEntry>
{
	@Override
	public int compare(ApacheLogEntry a, ApacheLogEntry b)
	{
		return a.getDate().compareTo(b.getDate());
	}
}