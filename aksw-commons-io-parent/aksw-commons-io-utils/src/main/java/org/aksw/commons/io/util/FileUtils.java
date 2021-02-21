package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
	
	/** Delete a path if it is an empty directory */
	public void deleteDirectoryIfEmpty(Path path) throws IOException {
		boolean isDirectory = Files.isDirectory(path);
		if (isDirectory) {
			boolean isEmpty = Files.list(path).count() == 0;
			if (isEmpty) {
				Files.delete(path);
			}
		}
	}
	
	/** TODO Add: Recursively delete all empty folders */
}
