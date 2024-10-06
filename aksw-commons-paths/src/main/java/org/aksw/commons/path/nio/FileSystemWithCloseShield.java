package org.aksw.commons.path.nio;

import java.io.IOException;
import java.nio.file.FileSystem;

public class FileSystemWithCloseShield
    extends FileSystemWrapper
{
    protected FileSystemWithCloseShield(FileSystem delegate) {
        super(delegate);
    }

    public static FileSystem of(FileSystem delegate) {
        return new FileSystemWithCloseShield(delegate);
    }

    @Override
    public void close() throws IOException {
        // No op
    }
}
