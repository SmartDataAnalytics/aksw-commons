package org.aksw.common.io.util.symlink;

import java.io.IOException;
import java.nio.file.Path;

public interface SymbolicLinkStrategy {
	boolean isSymbolicLink(Path path);
	void createSymbolicLink(Path link, Path target) throws IOException;
	Path readSymbolicLink(Path link) throws IOException;
}
