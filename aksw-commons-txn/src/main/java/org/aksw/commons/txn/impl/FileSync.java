package org.aksw.commons.txn.impl;

import java.nio.file.Path;

public interface FileSync
    extends ContentSync
{
    /** Get the path to the file affected by the sync */
    Path getTargetFile();

    /** Get the file that represents the new content */
    Path getNewContentTmpFile();
}
