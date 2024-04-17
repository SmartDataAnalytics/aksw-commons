package org.aksw.commons.txn.impl;

import org.aksw.commons.path.core.Path;

/**
 * Callbacks for reacting to events during the life cycle management of transactions.
 *
 * The most relevant callback is {@link #beforeUnlock(Path, boolean)}: At this point
 * resources have been written and are in their final location but the lock is still held.
 * This allows one to e.g. safely read the last modified timestamp of a just committed file
 * before another transaction can modify it again.
 */
public interface TxnHandler {
    default void beforePreCommit(Path<String> resKey) throws Exception { };
    default void afterPreCommit(Path<String> resKey) throws Exception { };
    default void beforeUnlock(Path<String> resKey, boolean isCommit) throws Exception { };
    default void end() { };
}
