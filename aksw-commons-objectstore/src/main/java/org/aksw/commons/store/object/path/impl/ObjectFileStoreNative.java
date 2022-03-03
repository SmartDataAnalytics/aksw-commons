package org.aksw.commons.store.object.path.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.store.object.path.api.ObjectFileStore;


public class ObjectFileStoreNative
    implements ObjectFileStore
{
    @Override
    public void write(Path target, Object obj) throws IOException {
        FileUtils.writeObject(target, obj);
    }

    @Override
    public Object read(Path source) throws IOException, ClassNotFoundException {
        return FileUtils.readObject(source);
    }

}
