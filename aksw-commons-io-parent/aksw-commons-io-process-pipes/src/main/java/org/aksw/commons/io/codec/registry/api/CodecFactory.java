package org.aksw.commons.io.codec.registry.api;

public interface CodecFactory {
	String getName();
	String getCategory();
	
	
	/**
	 * Report whether the codec is available.
	 * This may check for the availability for a system command such as lbzip.
	 * 
	 * @return
	 */
	boolean isAvailable();
}
