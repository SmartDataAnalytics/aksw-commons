package org.aksw.commons.io.input;

import java.io.IOException;

public interface HasPosition {
    long position() throws IOException;
    void position(long pos) throws IOException;
}
