package org.aksw.commons.util.range;

import com.google.common.collect.BoundType;

public class BoundTypeUtils {
	public static BoundType toggle(BoundType boundType) {
		BoundType r;
		switch (boundType) {
		case OPEN: r = BoundType.CLOSED; break;
		case CLOSED: r = BoundType.OPEN; break;
		default: throw new RuntimeException("Should not happen");
		}
		return r;
	}
}
