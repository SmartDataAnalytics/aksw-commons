package org.aksw.commons.util.factory;

import java.util.List;

public interface FactoryN<T> {
	T create(List<T> args);
}
