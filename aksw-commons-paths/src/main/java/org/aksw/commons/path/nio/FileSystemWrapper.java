package org.aksw.commons.path.nio;

import java.nio.file.FileSystem;

public class FileSystemWrapper
    extends FileSystemWrapperBase
{
    protected FileSystem delegate;

    public FileSystemWrapper(FileSystem delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected FileSystem getDelegate() {
        return delegate;
    }
}
