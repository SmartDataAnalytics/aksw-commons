package org.aksw.commons.util.range;


/** Idea to indirect access to buffers via an accessor interface
 * for efficient byte - object handling
 */
public interface BufferAccessor<B, T> {
    long position(B buffer);
    void position(B buffer, long newPosition);



    void get(B buffer, long index);
    void write(B buffer, T item);
    void write(B buffer, T[] items, long offset);
}
