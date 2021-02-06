package org.aksw.commons.io.process.pipe;

import java.io.InputStream;
import java.nio.file.Path;

import org.aksw.commons.io.endpoint.FileCreation;

public interface ProcessSink {
	InputStream getInputStream();
	FileCreation redirectTo(Path path);
}
