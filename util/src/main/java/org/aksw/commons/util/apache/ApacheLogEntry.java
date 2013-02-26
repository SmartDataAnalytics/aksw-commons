package org.aksw.commons.util.apache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheLogEntry {
	
	private static final int NUM_FIELDS = 9;
	private static final Pattern logEntryPattern = Pattern.compile("^([^\\s]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"");
    
	// 17/Apr/2011:06:47:47 +0200
	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

	
	private String hostname;
	private Date date;
	private ApacheLogRequest request;
	private String response;
	private long byteCount;
	private String referer;
	private String userAgent;
		
	/*
	public ApacheLogEntry(String hostname, Date date, String request,
			String response, String byteCount, String referer, String userAgent)
	{
		super();
		this.hostname = hostname;
		this.date = date;
		this.request = request;
		this.response = response;
		this.byteCount = byteCount;
		this.referer = referer;
		this.userAgent = userAgent;
	}*/
	
	public String getHostname()
	{
		return hostname;
	}

	public Date getDate()
	{
		return date;
	}

	public ApacheLogRequest getRequest()
	{
		return request;
	}
	
	
	public String getResponse()
	{
		return response;
	}

	public long getByteCount()
	{
		return byteCount;
	}

	public String getReferer()
	{
		return referer;
	}

	public String getUserAgent()
	{
		return userAgent;
	}
	

	ApacheLogEntry(String logEntryLine) throws ParseException
	{
	    Matcher matcher = logEntryPattern.matcher(logEntryLine);
	    if (!matcher.matches()) {
	    	throw new ParseException("No matches found when parsing line: " + logEntryLine, 0);
	    }
	    
	    if (NUM_FIELDS != matcher.groupCount()) {
	    	throw new ParseException("Error parsing line: " + logEntryLine + " ; groupCount = " + matcher.groupCount(), 0);
	    }
	    
	    this.hostname = matcher.group(1);
	    this.date = dateFormat.parse(matcher.group(4));
	    this.request = ApacheLogRequest.parse(matcher.group(5));
	    this.response = matcher.group(6);
	    this.byteCount = Long.parseLong(matcher.group(7));

	    this.referer = "";
	    if (!matcher.group(8).equals("-")) {
	    	this.referer =  matcher.group(8);
	    }
	    this.userAgent = matcher.group(9);
	}

	public static ApacheLogEntry parse(String logEntryLine)
		throws ParseException
	{
		return new ApacheLogEntry(logEntryLine);
	}
}