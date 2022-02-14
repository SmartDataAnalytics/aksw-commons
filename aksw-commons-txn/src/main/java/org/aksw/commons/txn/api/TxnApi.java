package org.aksw.commons.txn.api;

import java.util.concurrent.Callable;

public interface TxnApi
	// extends AutoCloseable
{
	void begin(boolean write);
	void commit();
	void abort();

	public static void execRead(TxnApi txn, Runnable runnable) {
		compute(txn, false, () -> { runnable.run(); return null; });
	}

	public static void execWrite(TxnApi txn, Runnable runnable) {
		compute(txn, true, () -> { runnable.run(); return null; });
	}
	
	public static <T> T execRead(TxnApi txn, Callable<T> callable) {
		return compute(txn, false, callable);
	}

	public static <T> T execWrite(TxnApi txn, Callable<T> callable) {
		return compute(txn, true, callable);
	}
	
	public static <T> T compute(TxnApi txn, boolean isWrite, Callable<T> callable) {
		T result;
		txn.begin(isWrite);
		
		// Ensure we don't call commit should a problem arise during abort!
		boolean success = false;
		try {
			result = callable.call();
			success = true;
		} catch (Exception e) {
			txn.abort();
			throw new RuntimeException(e);
		}
		
		if (success) {
			txn.commit();
		}
		
		return result;
	}
}
