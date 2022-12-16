package org.aksw.commons.util.closeable;

public class AutoCloseables {
	/**
	 * Utility method to close the given argument and re-throw any exception as a runtime one.
	 * Null arguments are ignored.
	 *
	 * @param closeable The closeable. May be null.
	 */
	public static void close(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
