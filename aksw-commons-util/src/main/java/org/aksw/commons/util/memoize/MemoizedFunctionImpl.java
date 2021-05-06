package org.aksw.commons.util.memoize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MemoizedFunctionImpl<I, O>
	implements MemoizedFunction<I, O>
{
	protected Map<I, O> cache;
	protected Function<I, O> delegate;
	
	public MemoizedFunctionImpl(Function<I, O> delegate, Map<I, O> cache) {
		super();
		this.delegate = delegate;
		this.cache = cache;
	}
	
	public Map<I, O> getCache() {
		return cache;
	}
	
	@Override
	public void clearCache() {
		cache.clear();
	}

	public O apply(I obj) {
		O result = cache.computeIfAbsent(obj, delegate);
		return result;
	}

	/** Create with backing by a ConcurrentHashMap */
	public static <I, O> MemoizedFunction<I, O> create(Function<I, O> delegate) {
		return new MemoizedFunctionImpl<>(delegate, new ConcurrentHashMap<>());
	}

	/** Create with backing by a HashMap */
	public static <I, O> MemoizedFunction<I, O> createNonConcurrent(Function<I, O> delegate) {
		return new MemoizedFunctionImpl<>(delegate, new HashMap<>());
	}
}
