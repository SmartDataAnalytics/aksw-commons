package org.aksw.commons.io.block.api;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.io.util.channel.ChannelFactory;

/**
 * A segment is a sequence of bytes with a fixed size.
 * Channels provide access to this sequence using a linear position.
 * Channels may maintain internal pointers corresponding to a position.
 * This may speed up speed up relative read operations as e.g. lookup of the right bucket
 * for consecutive reads can be skipped.
 *
 * @author raven
 */
public interface Segment
    extends ChannelFactory<Seekable>
{
    /**
     * Retrieve the length of the segment
     * For segments that are based on encoded data this method may trigger a full read.
     * Therefore, this method should be used with care.
     *
     * @return
     * @throws IOException
     */
    long length() throws IOException;

    /**
     * Return a sub-segment
     *
     * @param start
     * @param end
     * @return
     */
    // Segment slice(long start, long end);

    /**
     * The horizon is the number of bytes from the offset of the segment that have been loaded
     *
     * @return
     */
    // long horizon();
}
