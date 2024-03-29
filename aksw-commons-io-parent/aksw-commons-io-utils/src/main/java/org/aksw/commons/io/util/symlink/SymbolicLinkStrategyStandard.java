package org.aksw.commons.io.util.symlink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Symlink strategy based on the standard methods
 * {@link Files#createSymbolicLink(Path, Path, java.nio.file.attribute.FileAttribute...)}
 * and
 * {@link Files#readSymbolicLink(Path)}
 * 
 * @author raven
 *
 */
public class SymbolicLinkStrategyStandard
	implements SymbolicLinkStrategy
{
	@Override
	public void createSymbolicLink(Path link, Path target) throws IOException {
		Files.createSymbolicLink(link, target);
	}
	
	@Override
	public Path readSymbolicLink(Path link) throws IOException {
		Path result = Files.readSymbolicLink(link);
		return result;
	}
	
	@Override
	public boolean isSymbolicLink(Path path) {
		boolean result = Files.isSymbolicLink(path);
		return result;
	}
}
