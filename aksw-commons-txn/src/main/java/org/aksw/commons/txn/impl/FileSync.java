package org.aksw.commons.txn.impl;

import java.nio.file.Path;

/**
 * Interface for tracking changes to a file.
 * Any change results in a copy of the original file; conversely, changes are NOT tracked on the level of byte ranges.
 *
 * Upon syncing, the original (old) file is replaced with the new one. This operation should use atomic move if supported.
 *
 */
public interface FileSync
    extends ContentSync
{
    /** Get the path to the file affected by the sync */
    Path getTargetFile();

    /** Get the path to the file that contains the original content */
    Path getOldContentPath();

    /** Get the path to file with the current content - can be the original file or the temp file */
    Path getCurrentPath();

    /** Get the file that represents the new content */
    Path getNewContentTmpFile();
}
