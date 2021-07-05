package org.aksw.commons.rx.range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class ObjectFileStoreKyro
    implements ObjectFileStore
{
    protected Kryo kryo;

    public ObjectFileStoreKyro(Kryo kryo) {
        super();
        this.kryo = kryo;
    }

    @Override
    public void write(Path target, Object obj) throws IOException {
        try (Output out = new Output(Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            kryo.writeClassAndObject(out, obj);
            out.flush();
        }
    }

    @Override
    public Object read(Path source) throws IOException, ClassNotFoundException {
        try (InputStream in = Files.newInputStream(source, StandardOpenOption.READ)) {
            return kryo.readClassAndObject(new Input(in));
        }
    }

}
