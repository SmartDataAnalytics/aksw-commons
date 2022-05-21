package org.aksw.commons.util.apache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.aksw.commons.collections.SinglePrefetchIterator;

public class ApacheLogEntryIterator
	extends SinglePrefetchIterator<ApacheLogEntry>
	implements AutoCloseable
{
	private BufferedReader reader;
	private boolean closeWhenDone;


	public ApacheLogEntryIterator(InputStream in, boolean closeWhenDone)
	{
		this.reader = new BufferedReader(new InputStreamReader(in));
	}

	public ApacheLogEntryIterator(BufferedReader reader)
	{
		this.reader = reader;
	}


	@Override
	protected ApacheLogEntry prefetch() throws Exception
	{
		String line;
		while((line = reader.readLine()) != null) {
			try {
				return ApacheLogEntry.parse(line);
			} catch(Throwable e) {
				// TODO Shouldn't happen, but the parser is sucky, so it happens
				e.printStackTrace();
			}
		}

		return finish();
	}

	@Override
	public void close()
	{
		if(closeWhenDone) {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}