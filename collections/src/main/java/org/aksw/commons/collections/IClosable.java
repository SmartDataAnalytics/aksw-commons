package org.aksw.commons.collections;

/**
 * An interface for closing stuff.
 * Maybe we shouldn't introduce the 100000000 version of this interface, and reuse
 * it from some common package.
 *
 */
public interface IClosable
{
	void close();
}