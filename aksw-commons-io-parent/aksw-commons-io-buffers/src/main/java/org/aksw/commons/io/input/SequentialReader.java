package org.aksw.commons.io.input;

import java.io.Closeable;
import java.io.IOException;

/**
 * A sequential reader allows repeated retrieval of arrays of consecutive items.
 * Sequential readers can be seen as as form of InputStream or as a form of batch-iterator.
 *
 * Akin to an InputStream, the SequentialReader interface does not provide a seek() method.
 * Usually there should be another factory that creates sequential readers
 * for given offsets. The reason is, that a sequential reader is typically backed by a stream of items
 * (such as a http response, or a sql/sparql result set) and that stream needs to be re-created when
 * jumping to arbitrary offsets.
 *
 *
 * @author Claus Stadler, Feb 17, 2022
 *
 * @param <A>
 */
public interface SequentialReader<A>
    extends Closeable
{
    int read(A array, int position, int length) throws IOException ;
}
