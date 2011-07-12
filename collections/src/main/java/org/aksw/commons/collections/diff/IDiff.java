package org.aksw.commons.collections.diff;

/**
 * @author Claus Stadler
 *
 *         Date: 7/12/11
 *         Time: 10:48 PM
 */
public interface IDiff<T>
{
	T getAdded();
	T getRemoved();
	T getRetained();
}
