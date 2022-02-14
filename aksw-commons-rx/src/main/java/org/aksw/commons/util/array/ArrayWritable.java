package org.aksw.commons.util.array;

import java.io.IOException;
import java.lang.reflect.Array;

/** Interface for putting an array of items into a sequence at a certain offset */
public interface ArrayWritable<A>
	extends HasArrayOps<A>
{	
	/** The method that needs to be implemented; all other methods default-delegate to this one. */
    void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException;
    
    default void put(long offset, Object item) throws IOException {
    	ArrayOps<A> arrayOps = getArrayOps();
    	A singleton = arrayOps.create(1);
    	arrayOps.set(singleton, 0, item);
    	putAll(offset, singleton);
    }

    default void putAll(long offset, Object arrayWithItemsOfTypeT, int arrOffset) throws IOException {
        putAll(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT) - arrOffset);
    }

    default void putAll(long offset, Object arrayWithItemsOfTypeT) throws IOException {
        putAll(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
    }
}
