package org.aksw.commons.util.string;

import java.util.List;

/**
 * A model to represent file names - comprising a base name, a content type part and a list of encodings
 * 
 * @author raven
 */
public interface FileName {	
	String getBaseName();
	String getContentPart();
	List<String> getEncodingParts();

	
	/** The concatenation of contentPart and encoding parts (joined with dot) without a preceding dot */
	String getExtension(boolean precedingDotIfNotEmpty);
	// Future extensions:
	// List<String> getAllParts();
	// List<String> getBaseNameParts();
}
