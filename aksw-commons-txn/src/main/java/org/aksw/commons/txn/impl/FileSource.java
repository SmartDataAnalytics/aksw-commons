package org.aksw.commons.txn.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface FileSource {
    boolean isSymlink();

    void setSymlink(Path target);
    Path getSymlink();

    InputStream openInputStream();
    OutputStream openOutputStream();
}
