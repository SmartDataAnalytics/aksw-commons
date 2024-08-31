package org.aksw.commons.io.hadoop.binseach.v2;

/**
 * position: byte or block offset
 * displacement: position in block; when not dealing with blocks it is always 0
 * data: The header data
 * isDataConsumed: true if there is no more data beyond what is in the 'data' array
 */
public record HeaderRecord(long position, int displacement, byte[] data, boolean isDataConsumed) {}
