package org.aksw.commons.io.cache;

/**
 * Modes for how a data producer thread should react to a lack of demand.
 * It may either pause or read ahead.
 * 
 * @author raven
 *
 */
public enum IdleMode {
	PAUSE,
	READ_AHEAD
}
