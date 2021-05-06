package org.aksw.commons.util.memoize;

import java.util.Map.Entry;
import java.util.function.BiFunction;

public interface MemoizedBiFunction<I1, I2, O>
	extends BiFunction<I1, I2, O>, Memoized<Entry<I1, I2>, O>
{
}

