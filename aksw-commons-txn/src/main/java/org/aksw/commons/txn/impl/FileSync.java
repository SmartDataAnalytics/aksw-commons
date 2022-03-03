package org.aksw.commons.txn.impl;

import java.nio.file.Path;

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
