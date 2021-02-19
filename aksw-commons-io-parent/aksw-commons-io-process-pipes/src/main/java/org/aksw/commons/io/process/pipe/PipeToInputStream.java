package org.aksw.commons.io.process.pipe;

import java.io.InputStream;
import java.io.OutputStream;

public interface PipeToInputStream {
	OutputStream getOutputStream();
	InputStream getInputStream();
}
