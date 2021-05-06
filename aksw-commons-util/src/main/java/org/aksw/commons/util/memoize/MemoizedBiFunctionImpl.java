package org.aksw.commons.util.memoize;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class MemoizedBiFunctionImpl<I1, I2, O>
	implements MemoizedBiFunction<I1, I2, O>
{
	protected Map<Entry<I1, I2>, O> cache;
	protected BiFunction<I1, I2, O> delegate;
	
	public MemoizedBiFunctionImpl(BiFunction<I1, I2, O> delegate, Map<Entry<I1, I2>, O> cache) {
		super();
		this.delegate = delegate;
		this.cache = cache;
	}
	
	public Map<Entry<I1, I2>, O> getCache() {
		return cache;
	}
	
	@Override
	public void clearCache() {
		cache.clear();
	}

	public O apply(I1 a, I2 b) {
		O result = cache.computeIfAbsent(new SimpleEntry<>(a, b), e -> delegate.apply(a, b));
		return result;
	}

	/** Create with backing by a ConcurrentHashMap */
	public static <I1, I2, O> MemoizedBiFunction<I1, I2, O> create(BiFunction<I1, I2, O> delegate) {
		return new MemoizedBiFunctionImpl<>(delegate, new ConcurrentHashMap<>());
	}

	/** Create with backing by a HashMap */
	public static <I1, I2, O> MemoizedBiFunction<I1, I2, O> createNonConcurrent(BiFunction<I1, I2, O> delegate) {
		return new MemoizedBiFunctionImpl<>(delegate, new HashMap<>());
	}
}
