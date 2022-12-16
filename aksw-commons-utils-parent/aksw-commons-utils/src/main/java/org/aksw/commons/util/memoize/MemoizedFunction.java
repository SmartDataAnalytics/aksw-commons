package org.aksw.commons.util.memoize;

import java.util.function.Function;

public interface MemoizedFunction<I, O>
	extends Function<I, O>, Memoized<I, O>
{
}

