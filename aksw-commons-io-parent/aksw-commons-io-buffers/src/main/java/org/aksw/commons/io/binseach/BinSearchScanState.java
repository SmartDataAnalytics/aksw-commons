package org.aksw.commons.io.binseach;


/**
 * State information for scanning a region once binary search has found an offset.
 * The end of the region is detected dynamically
 *
 * TODO Generalize to arbitrary patterns
 *
 * @author raven
 *
 */
public class BinSearchScanState {
    public long firstDelimPos; // The match position found by scanning backwards with Pattern.match
    public long matchDelimPos; // The match position found by binary search
    public byte[] prefixBytes; // Generalize using lambda with a compatible signature to Pattern.match
    public long size;          // Absolute end of the data region on which the match was run
}