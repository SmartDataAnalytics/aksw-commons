package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

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
	
	/** TODO Add: Recursively delete all empty folders 
	 * @throws IOException */
	
	
	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/home/raven/tmp/lsq/data/dbpedia/test");

		List<Path> paths = listPaths(path, "part*");
		Collections.sort(paths, (a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()));
		System.out.println("Merging: " + paths);
		
		Stopwatch sw = Stopwatch.createStarted();
		FileMerger merger = FileMerger.create(
				Paths.get("/tmp/merged.trig"),
				paths);
		merger.addProgressListener(self -> System.out.println("Progress: " + self.getProgress()));
		merger.run();
		
		System.out.println(sw.elapsed(TimeUnit.SECONDS));
		
	}
	
	
	/**
	 * Return a list of files matching a given glob pattern and a base path
	 * 
	 * @param basePath
	 * @param glob
	 * @return
	 * @throws IOException
	 */
    public static List<Path> listPaths(Path basePath, String glob) throws IOException {
    	List<Path> result = null;
    	try(DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, glob)) {
    		result = Lists.newArrayList(stream.iterator());
    	}
    	return result;
    }

}
