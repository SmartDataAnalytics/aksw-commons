package org.aksw.commons.store.object.path.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ObjectSerializer {
	public void write(OutputStream out, Object obj) throws IOException;
	public Object read(InputStream in) throws IOException;
}
