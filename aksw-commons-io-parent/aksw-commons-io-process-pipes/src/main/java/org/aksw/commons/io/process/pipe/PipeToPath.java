package org.aksw.commons.io.process.pipe;

import java.io.OutputStream;

import org.aksw.commons.io.endpoint.FileCreation;

public interface PipeToPath {
	OutputStream getOutputStream();
	FileCreation getFileCreation();
}
