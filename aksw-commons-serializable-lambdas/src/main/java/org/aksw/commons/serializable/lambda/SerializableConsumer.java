package org.aksw.commons.serializable.lambda;

import java.io.Serializable;
import java.util.function.Consumer;

public interface SerializableConsumer<T> extends Consumer<T>, Serializable {}