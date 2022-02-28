package org.aksw.commons.buffer.range;

import java.io.IOException;

/**
 * Exception used with {@link RangeBuffer} when attempting to read a range of data
 * which there are one or more gaps in the buffer.
 * Read operations should typically be scheduled w.r.t. available data, however
 * concurrent modifications may invalidate such schedules and re-scheduling based on this
 * exception is a simple way to react to such changes.
 *  
 */
public class ReadOverGapException
	extends IOException
{
	private static final long serialVersionUID = 1L;

	public ReadOverGapException() {
		super();
	}

	public ReadOverGapException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReadOverGapException(String message) {
		super(message);
	}

	public ReadOverGapException(Throwable cause) {
		super(cause);
	}	
}
