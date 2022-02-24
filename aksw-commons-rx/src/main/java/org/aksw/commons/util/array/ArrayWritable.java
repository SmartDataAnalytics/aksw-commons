package org.aksw.commons.util.array;

import java.io.IOException;
import java.lang.reflect.Array;

/** Interface for putting an array of items into a sequence at a certain offset */
public interface ArrayWritable<A>
	extends HasArrayOps<A>
{
	/** The method that needs to be implemented; all other methods default-delegate to this one. */
    void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException;

    default void put(long offset, A item) throws IOException {
    	ArrayOps<A> arrayOps = getArrayOps();
    	A singleton = arrayOps.create(1);
    	arrayOps.set(singleton, 0, item);
    	write(offset, singleton);
    }

    default void write(long offset, A arrayWithItemsOfTypeT, int arrOffset) throws IOException {
        write(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT) - arrOffset);
    }

    default void write(long offset, A arrayWithItemsOfTypeT) throws IOException {
        write(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
    }
}
