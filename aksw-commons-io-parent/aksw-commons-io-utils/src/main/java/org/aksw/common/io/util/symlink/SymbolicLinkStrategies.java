package org.aksw.common.io.util.symlink;

public class SymbolicLinkStrategies {
	public static final SymbolicLinkStrategy STANDARD = new SymbolicLinkStrategyStandard();
	public static final SymbolicLinkStrategy FILE = SymbolicLinkStrategyFile.createDefault();
	public static final SymbolicLinkStrategy FILE_DYSNC = SymbolicLinkStrategyFile.createDsync();
}
