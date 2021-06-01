package org.aksw.commons.tuple;


/**
 * An extension of tuple accessor which allows for access via keys (in addition to ordinal integers).
 * 
 * @author raven
 *
 * @param <D>
 * @param <C>
 * @param <K>
 */
public interface GenericTupleAccessor<D, C, K>
	extends TupleAccessor<D, C>
{
	/** Map an ordinal to a key */
	K keyAtOrdinal(int index);
	
	int ordinalOfKey(K key);
	
	/** Get value by key */
	C get(D tupleLike, K key); // Object rather than K?
}