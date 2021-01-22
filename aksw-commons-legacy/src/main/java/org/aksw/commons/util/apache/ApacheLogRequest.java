package org.aksw.commons.util.apache;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheLogRequest
{
	private static final Pattern requestPattern = Pattern.compile("^([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)"); 
	private String method;
	private String url;
	private String protocol;
	
	ApacheLogRequest(String str)
		throws ParseException
	{
	    Matcher matcher = requestPattern.matcher(str);
	    if (!matcher.matches()) {
	    	throw new ParseException("No matches found when parsing request: " + str, 0);
	    }
	    
	    if (3 != matcher.groupCount()) {
	    	throw new ParseException("Error parsing request: " + str + " ; groupCount = " + matcher.groupCount(), 0);
	    }
	    
	    this.method = matcher.group(1);
	    this.url = matcher.group(2);		
	    this.protocol = matcher.group(3);		
	}
	
	public static ApacheLogRequest parse(String str)
		throws ParseException
	{
		return new ApacheLogRequest(str);
	}
	
	public ApacheLogRequest(String method, String url, String protocol)
	{
		super();
		this.method = method;
		this.url = url;
		this.protocol = protocol;
	}
	
	public String getMethod()
	{
		return method;
	}
	public String getUrl()
	{
		return url;
	}
	public String getProtocol()
	{
		return protocol;
	}
}