package org.aksw.commons.rx.cache.range;

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
