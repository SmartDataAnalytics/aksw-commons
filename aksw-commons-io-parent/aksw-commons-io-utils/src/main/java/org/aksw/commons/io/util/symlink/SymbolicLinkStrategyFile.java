package org.aksw.commons.io.util.symlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;


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
	protected OpenOption[] openOptions;
	
	public static SymbolicLinkStrategyFile createDefault() {
		return new SymbolicLinkStrategyFile(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
	}
	
	// Super slow but should reliable work in case of system crashes
	public static SymbolicLinkStrategyFile createDsync() {
		return new SymbolicLinkStrategyFile(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
	}

	public SymbolicLinkStrategyFile(OpenOption... openOptions) {
		super();
		this.openOptions = openOptions;
	}

	
	@Override
	public void createSymbolicLink(Path link, Path target) throws IOException {
		byte[] bytes = target.toString().getBytes(StandardCharsets.UTF_8);
		try (OutputStream out = Files.newOutputStream(link, openOptions)) {
			out.write(bytes);			
			out.flush();
		}
		
// FIXME Not all VFS support file channel
//		try (FileChannel fc = FileChannel.open(link, openOptions)) {
//			fc.write(ByteBuffer.wrap(bytes));
//			
//			if (Arrays.asList(openOptions).contains(StandardOpenOption.DSYNC)) {
//				fc.force(true); // Is that needed if we use DSYNC?
//			}
//		}
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException {
		// readAllBytes uses FileChannels which are often not implemented in VFS
		// (in my case it wouldn't work for WebDAV)
		// For this reason we use an approach that relies on InputStream
//		byte[] bytes = Files.readAllBytes(link);
		
		try (InputStream in = Files.newInputStream(link)) {
			byte[] bytes = IOUtils.toByteArray(in);
			String str = new String(bytes, StandardCharsets.UTF_8);
			Path result = Paths.get(str);
			return result;
		}
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		boolean result = Files.exists(path, LinkOption.NOFOLLOW_LINKS);
		return result;
	}


}
