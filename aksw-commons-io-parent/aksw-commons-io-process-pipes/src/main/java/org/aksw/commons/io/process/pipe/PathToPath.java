package org.aksw.commons.io.process.pipe;

import java.nio.file.Path;

import org.aksw.commons.io.endpoint.FileCreation;

public interface PathToPath {
	FileCreation apply(Path in, Path out);
}
