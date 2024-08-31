package org.aksw.commons.io.input;

import java.io.IOException;

@FunctionalInterface
public interface SetPosition { void accept(long position) throws IOException; }
