package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.stream.Collector;

public interface SerializableCollector<T, A, R> extends Collector<T, A, R>, Serializable {}