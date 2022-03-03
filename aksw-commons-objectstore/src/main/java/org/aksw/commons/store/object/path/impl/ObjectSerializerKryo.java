package org.aksw.commons.store.object.path.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.commons.store.object.path.api.ObjectSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

public class ObjectSerializerKryo
	implements ObjectSerializer
{
    protected KryoPool kryoPool;
    // TODO Add support for a (hadoop) codec pool

	protected ObjectSerializerKryo(KryoPool kryoPool) {
		super();
		this.kryoPool = kryoPool;
	}

	public static ObjectSerializerKryo create(KryoPool kryoPool) {
		return new ObjectSerializerKryo(kryoPool);
	}

	@Override
	public void write(OutputStream out, Object obj) throws IOException {
        Kryo kryo = kryoPool.borrow();
        Output o = new Output(out);
        kryo.writeClassAndObject(o, obj);
        o.flush();
        kryoPool.release(kryo);
	}

	@Override
	public Object read(InputStream in) throws IOException {
        Kryo kryo = kryoPool.borrow();
        try {
            return kryo.readClassAndObject(new Input(in));
        } finally {
            kryoPool.release(kryo);
        }
	}

}
