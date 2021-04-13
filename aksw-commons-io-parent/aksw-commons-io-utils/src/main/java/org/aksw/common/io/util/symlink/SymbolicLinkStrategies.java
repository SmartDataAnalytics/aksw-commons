package org.aksw.common.io.util.symlink;

public class SymbolicLinkStrategies {
	public static final SymbolicLinkStrategy STANDARD = new SymbolicLinkStrategyStandard();
	public static final SymbolicLinkStrategy FILE = new SymbolicLinkStrategyFile();
}
