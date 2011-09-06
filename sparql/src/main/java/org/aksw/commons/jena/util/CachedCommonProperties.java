package org.aksw.commons.jena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Range;
import net.sf.oval.guard.Guarded;

/** Cached version of {@link CommonProperties}.
 * The constructor tries to load the cache from the given file. If the file does not exist, an empty cache is created and 
 * filled with every cache miss. Call {@link save()} to write the cache on disk. Set {@link offline} to true if you want to 
 * use the cached values only. Values from the cache are used regardless of the original parameters for {@link threshold}, {@link endpoint} and {@link sampleSize}.
 *  @author Konrad Höffner */
@Guarded(applyFieldConstraintsToConstructors=true)
public class CachedCommonProperties
{
	private final Map<Integer,LinkedHashMap<String,Integer>> whereToProperties;
	private final @NotNull File cacheFile;
	private final @NotNull @NotEmpty String endpoint;
	private final @Range(min=0, max=1) Double threshold;
	private final @Min(1) Integer maxResultSize;
	private final @Min(1) Integer sampleSize;

	public boolean offline = false;
	
	public LinkedHashMap<String,Integer> getCommonProperties(@NotNull @NotEmpty String where)
	{
		int hash = where.hashCode();
		LinkedHashMap<String,Integer> properties = whereToProperties.get(hash);
		if(properties==null&&!offline)
		{
			properties = CommonProperties.getCommonProperties(endpoint, where, threshold, maxResultSize, sampleSize);
			whereToProperties.put(hash,properties);
		}
		return properties;
	}
	
	@SuppressWarnings("unchecked")
	public CachedCommonProperties(File cacheFile, String endpoint, Double threshold, Integer maxResultSize, Integer sampleSize) throws IOException
	{
		this.cacheFile = cacheFile;
		this.endpoint = endpoint;
		this.threshold = threshold;
		this.maxResultSize = maxResultSize;
		this.sampleSize = sampleSize;
		if(cacheFile.exists())
		{
			whereToProperties = load();
		} else
		{
			whereToProperties = new HashMap<Integer,LinkedHashMap<String,Integer>>();
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<Integer,LinkedHashMap<String,Integer>> load() throws IOException
	{
		InputStream fis = null;
		fis = new FileInputStream( cacheFile);
		ObjectInputStream o = new ObjectInputStream( fis );
		try
		{
			return (HashMap<Integer, LinkedHashMap<String, Integer>>) o.readObject();
		} catch (ClassNotFoundException e)	{e.printStackTrace();return new HashMap<Integer,LinkedHashMap<String,Integer>>();}
	}

	public void save() throws IOException
	{
		OutputStream fos = new FileOutputStream( cacheFile);
		ObjectOutputStream o = new ObjectOutputStream( fos );
		synchronized(whereToProperties) {	o.writeObject(whereToProperties);}
		fos.close();
	}
}