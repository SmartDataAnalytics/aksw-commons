package org.aksw.commons.store.object.path.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.store.object.path.api.ObjectFileStore;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;


public class ObjectFileStoreKryo
    implements ObjectFileStore
{
    protected KryoPool kryoPool;
    // TODO Add support for a (hadoop) codec pool

    public ObjectFileStoreKryo(KryoPool kryoPool) {
        super();
        this.kryoPool = kryoPool;
    }

    @Override
    public void write(Path target, Object obj) throws IOException {
        Kryo kryo = kryoPool.borrow();
        try (Output out = new Output(Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            kryo.writeClassAndObject(out, obj);
            out.flush();
        } finally {
            kryoPool.release(kryo);
        }
    }

    @Override
    public Object read(Path source) throws IOException, ClassNotFoundException {
        Kryo kryo = kryoPool.borrow();
        try (InputStream in = Files.newInputStream(source, StandardOpenOption.READ)) {
            return kryo.readClassAndObject(new Input(in));
        } finally {
            kryoPool.release(kryo);
        }
    }

}
