package org.aksw.commons.path.nio;

import java.nio.file.FileSystem;

public class FileSystemWithSpec
    extends FileSystemWrapper
{
    protected FileSystemSpec spec;

    public FileSystemWithSpec(FileSystem delegate, FileSystemSpec spec) {
        super(delegate);
        this.spec = spec;
    }

    public FileSystemSpec getSpec() {
        return spec;
    }
}
