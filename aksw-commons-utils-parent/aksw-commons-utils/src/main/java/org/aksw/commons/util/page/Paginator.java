package org.aksw.commons.util.page;

import java.util.List;

public interface Paginator<T> {
	List<T> createPages(long numItems, long itemOffset);
}