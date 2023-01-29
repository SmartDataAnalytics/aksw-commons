package org.aksw.commons.tuple.accessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GenericTupleAccessorForMap<C, K>
	extends GenericTupleAccessorFromListOfKeysBase<Map<K, C>, C, K>
{
	protected Supplier<? extends Map<K, C>> mapSupplier;
	
	public GenericTupleAccessorForMap(List<K> keys) {
		this(keys, HashMap::new);
	}

	public GenericTupleAccessorForMap(List<K> keys, Supplier<? extends Map<K, C>> mapSupplier) {
		super(keys);
		this.mapSupplier = mapSupplier;
	}

	@Override
	public <T> Map<K, C> build(T obj, TupleAccessor<? super T, ? extends C> accessor) {
		Map<K, C> result = mapSupplier.get();
		
		int n = getDimension();
		for (int i = 0; i < n; i++) {
			K key = keyAtOrdinal(i);
			C value = accessor.get(obj, i);
			
			if (value != null) {
				result.put(key, value);
			}
		}
		return result;
	}

	@Override
	public C get(Map<K, C> tupleLike, K key) {
		C result = tupleLike.get(key);
		return result;
	}

}
