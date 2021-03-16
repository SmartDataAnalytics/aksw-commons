package org.aksw.commons.beans.model;

import java.util.Collection;
import java.util.Iterator;


@SuppressWarnings("rawtypes")
public class CollectionOpsCollection
	implements CollectionOps
{
	@Override
	public Iterator<?> getItems(Object entity) {
		Collection c = (Collection)entity;
		Iterator<?> result = c.iterator();
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setItems(Object entity, Iterator<?> items) {
		Collection c = (Collection)entity;
		
		while (items.hasNext()) {
			Object o = items.next();
			c.add(o);
		}
//		Iterators.addAll(c, items);
	}
}
