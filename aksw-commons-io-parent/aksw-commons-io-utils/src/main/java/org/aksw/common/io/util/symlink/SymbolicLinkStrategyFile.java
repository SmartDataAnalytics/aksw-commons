package org.aksw.common.io.util.symlink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 * Symlink strategy that creates ordinary files whose content is the string
 * representation of the target path being linked to.
 * 
 * @author raven
 *
 */
public class SymbolicLinkStrategyFile
	implements SymbolicLinkStrategy
{
	@Override
	public void createSymbolicLink(Path link, Path target) throws IOException {
		byte[] bytes = target.toString().getBytes(StandardCharsets.UTF_8);
		try (FileChannel fc = FileChannel.open(link,
				StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.DSYNC)) {
			fc.write(ByteBuffer.wrap(bytes));
			fc.force(true); // Is that needed if we use DSYNC?
		}
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException {
		byte[] bytes = Files.readAllBytes(link);
		String str = new String(bytes, StandardCharsets.UTF_8);
		Path result = Paths.get(str);
		return result;
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		boolean result = Files.exists(path, LinkOption.NOFOLLOW_LINKS);
		return result;
	}


}
