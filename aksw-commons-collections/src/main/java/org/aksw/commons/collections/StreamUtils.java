package org.aksw.commons.collections;

import java.util.stream.Stream;

public class StreamUtils {
    public static <T> T expectOneItem(Stream<T> stream) {
    	try {
    		return IteratorUtils.expectOneItem(stream.iterator());
    	} finally {
    		stream.close();
    	}
    }

    public static <T> T expectZeroOrOneItems(Stream<T> stream) {
    	try {
    		return IteratorUtils.expectZeroOrOneItems(stream.iterator());
    	} finally {
    		stream.close();
    	}
    }
}
