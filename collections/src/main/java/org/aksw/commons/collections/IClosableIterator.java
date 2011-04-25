package org.aksw.commons.collections;

import java.util.Iterator;

public interface IClosableIterator<T>
	extends Iterator<T>, IClosable
{
}