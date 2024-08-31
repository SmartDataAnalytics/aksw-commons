package org.aksw.commons.io.input;

import java.io.IOException;

@FunctionalInterface
public interface GetPosition { long call() throws IOException; }
