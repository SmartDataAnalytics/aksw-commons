package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Merges the byte content of a list of files into a single file.
 * Use {@link #create(Path, List)} to set up an instance of this class.
 * Use {@link #run()} to perform the merge.
 * Calling run while another thread is already running raises an {@link IllegalStateException}.
 * Run be called in succession in which the process is repeated.
 * 
 * @author Claus Stadler
 */
public class FileMerger
{
	protected List<PathTransfer> pathTransfers;
	protected Path destPath;
	protected long totalTransferSize;

	protected Thread runThread = null;
	protected long transferred = 0;
	
	protected Collection<Consumer<? super FileMerger>> progressListeners = Collections.synchronizedList(new LinkedList<>());
	
	public FileMerger(Path destPath, List<PathTransfer> pathTransfers, long totalTransferSize) {
		super();
		this.destPath = destPath;
		this.pathTransfers = pathTransfers;
		this.totalTransferSize = totalTransferSize;
	}

	public Runnable addProgressListener(Consumer<? super FileMerger> progressListener) {
		progressListeners.add(progressListener);
		return () -> progressListeners.remove(progressListener);
	}
	
	public void notifyProgressListeners() {
		for (Consumer<? super FileMerger> listener : progressListeners) {
			listener.accept(this);
		}
	}
	
	/** Convenience method that return the ration of transferred bytes and total bytes */
	public double getProgress() {
		return transferred / (double)totalTransferSize;
	}
	
	public void abort() {
		synchronized (this) {
			if (runThread != null) {
				runThread.interrupt();
			}
		}
	}
	
	public void run() throws IOException {
		synchronized(this) {
			if (runThread != null) {
				throw new IllegalStateException("A merge task is already running");
			}
			
			runThread = Thread.currentThread();
		}
		
		transferred = 0;				
		try (FileChannel out = FileChannel.open(destPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			for (PathTransfer pathTransfer : pathTransfers) {
				FileChannelUtils.transferFromFully(out, pathTransfer, contrib -> {
					this.transferred += contrib;
					notifyProgressListeners();
				});
			}
		} finally {
			synchronized (this) {
				runThread = null;
			}
		}
	}

	/**
	 * Merge a sequence of files into a single destination file.
	 * The destination must not yet exist.
	 * 
	 * @param dest
	 * @param srcPaths
	 * @throws IOException
	 */
	public static FileMerger create(Path destPath, List<Path> srcPaths) throws IOException {
		// Compute offsets for each part; implicitly checks whether the paths exist
		List<PathTransfer> pathTransfers = new ArrayList<>(srcPaths.size());
		long nextOffset = 0;
		for (int i = 0; i < srcPaths.size(); ++i) {
			Path srcPath = srcPaths.get(i);
			long contrib = Files.size(srcPath);
			pathTransfers.add(new PathTransfer(srcPath, nextOffset, contrib));
			nextOffset += contrib;
		}

		return new FileMerger(destPath, pathTransfers, nextOffset);
	}
	
	
//  Experimenting with multi threading; for some reason this just hangs
//	try (FileChannel out = FileChannel.open(dest, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {					
//	}
//	
//	pathTransfers.parallelStream().peek(pathTransfer -> {
//		try {
//			transferFrom(dest, pathTransfer);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		
//	}).count();
//


//	public static void transferFrom(Path destPath, PathTransfer pathTransfer) throws IOException {
//		try (FileChannel out = FileChannel.open(destPath, StandardOpenOption.WRITE)) {					
//			transferFrom(out, pathTransfer);
//		}
//	}


}
