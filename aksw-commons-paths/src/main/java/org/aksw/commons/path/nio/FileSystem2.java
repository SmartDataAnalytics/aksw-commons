package org.aksw.commons.path.nio;

import java.io.IOException;
import java.nio.file.FileSystem;

/** A nested file system. Closing the outer one also recursively closes the inner ones. */
public class FileSystem2
    extends FileSystemWrapper
{
    protected FileSystem underlying;

    public FileSystem2(FileSystem delegate, FileSystem underlying) {
        super(delegate);
        this.underlying = underlying;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            underlying.close();
        }
    }
}
