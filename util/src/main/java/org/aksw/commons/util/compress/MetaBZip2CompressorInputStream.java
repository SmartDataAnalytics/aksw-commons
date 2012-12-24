package org.aksw.commons.util.compress;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;


public class MetaBZip2CompressorInputStream
	extends InputStream
{
	private InputStream is;
	private BZip2CompressorInputStream current;

	//private static final InputStream emptyStream = new ByteArrayInputStream(new byte[]{});
	
	private boolean isDone = false;
	
	public MetaBZip2CompressorInputStream(InputStream is)
		throws IOException
	{
		this.is = is;
		this.current = new BZip2CompressorInputStream(is);
	}
	
	void recreate()
		throws IOException
	{		
		//if(current != null)
		//	current.close();
		
		if(is.available() == 0) {
			isDone = true;
			return;
		}

		current = new BZip2CompressorInputStream(is);  
	}
	
	@Override
	public int read()
		throws IOException
	{
		int result = current.read();
		
		if(result == -1) {
			recreate();			
			result = isDone ? result : current.read();
		}

		return result;
	}
	
	@Override
	public int read(byte[] dest, int offs, int len)
		throws IOException
	{
		int result = current.read(dest, offs, len);

		if(result == -1) {
			recreate();			
			result = isDone ? result : current.read(dest, offs, len);
		}
		
		return result;
	}

	@Override
	public int read(byte[] dest)
		throws IOException
	{
		int result = current.read(dest);
		
		if(result == -1) {
			recreate();
			result = isDone ? result : current.read(dest);
		}
		
		return result;
	}
}
