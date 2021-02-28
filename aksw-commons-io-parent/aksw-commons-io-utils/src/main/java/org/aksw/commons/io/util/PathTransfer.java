package org.aksw.commons.io.util;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * Helper class for use with {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}
 * Maps length bytes of the given path to some target with the given offset.
 * 
 */
public class PathTransfer {
	protected Path path;
	protected long length;
	
	protected long targetOffset;

	public PathTransfer(Path path, long targetOffset, long length) {
		super();
		this.path = path;
		this.targetOffset = targetOffset;
		this.length = length;
	}

	public Path getPath() {
		return path;
	}

	public long getTargetOffset() {
		return targetOffset;
	}

	public long getLength() {
		return length;
	}
}
