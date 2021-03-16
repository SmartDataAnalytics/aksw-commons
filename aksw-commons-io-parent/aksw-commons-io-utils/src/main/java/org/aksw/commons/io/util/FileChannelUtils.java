package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class FileChannelUtils {

	/** Invoke transferFrom on dest with arguments based on pathTransfer */
	public static long transferFromFully(FileChannel dest, PathTransfer pathTransfer, Consumer<Long> contribCallback) throws IOException {
		return transferFromFully(dest, pathTransfer.getPath(), pathTransfer.getTargetOffset(), pathTransfer.getLength(), contribCallback);
	}

	/** Invoke transferFrom on dest it with the given arguments; opens a file channel on src */
	public static long transferFromFully(FileChannel destChannel, Path srcPath, long targetOffset, long length, Consumer<Long> contribCallback) throws IOException {
		try (FileChannel srcChannel = FileChannel.open(srcPath, StandardOpenOption.READ)) {
			return transferFromFully(destChannel, srcChannel, targetOffset, length, contribCallback);
		}	
	}

	/** 
	 * Repeatedly invoke {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)} 
	 * until length bytes were transferred.
	 * 
	 * @param destChannel The channel to write to
	 * @param srcChannel The channel to read from
	 * @param destOffset The offset in the the destChannel where to add data from srcChannel
	 * @param length The number of bytes to transfer from srcChannel to destChannel
	 * @return The actual number of bytes transferred
	 */
	public static long transferFromFully(
			FileChannel destChannel,
			FileChannel srcChannel,
			long destOffset,
			long length,
			Consumer<Long> contribCallback) throws IOException {
		// System.out.println(String.format("Offset %d: Receiving %d bytes from %s", targetOffset, length, srcPath.toString()));
		long transferred = 0;
		
		int zeroCount = 0;
		Thread currentThread = Thread.currentThread();
		while (transferred < length && !currentThread.isInterrupted()) {
			long remainingLength = length - transferred;
			long n = destChannel.transferFrom(srcChannel, destOffset + transferred, remainingLength);
			
			// If we repeatedly get 0 then check whether maybe the length parameter was set too high
			// Another reason may be that the srcChannel was shrunk concurrently
			// In any case prevent an endless loop
			if (n == 0) {
				if (++zeroCount >= 10) {
					long srcChannelSize = srcChannel.size();
					if (length > srcChannelSize) {
						length = srcChannelSize;
					}
				}
			} else {
				zeroCount = 0;
			}
			
			transferred += n;
			srcChannel.position(transferred);
			
			if (contribCallback != null) {
				contribCallback.accept(n);
			}
		}
		
		return transferred;
	}

}
