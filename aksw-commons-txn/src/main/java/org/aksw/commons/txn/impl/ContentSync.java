package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.function.Consumer;

public interface ContentSync
    extends TxnComponent
{

    /**
     * Read the most recent content (maybe open the original file).
     * Beware that calling {@link #newOutputStreamToNewTmpContent()} may practically invalidate the returned input stream. */
    InputStream openCurrentContent() throws IOException;

    /**
     * Open an output stream to the tmp content.
     * If the new content is currently a symlink then it gets deleted and replaced with a regular file
     */
    OutputStream newOutputStreamToNewTmpContent(boolean truncate) throws IOException;


    /** Replaces the new tmp content with a symlink */
//    void setNewTmpContentToSymlink(Path target) throws IOException;

//    boolean isCurrentContentASymLink();
//    Path readSymlinkFromCurrentContent() throws IOException;


    /** Returns true iff there is at least one backing resource with the old or new state */
    boolean exists();

    Instant getLastModifiedTime() throws IOException;

    /**
     * Set the new content of a resource.
     * The new content is not committed.
     *
     * @param outputStreamSupplier
     * @throws IOException
     */
    void putContent(Consumer<OutputStream> outputStreamSupplier) throws IOException;

    void recoverPreCommit() throws IOException;

    default void markForDeletion() throws IOException {
        try (OutputStream out = newOutputStreamToNewTmpContent(true)) { }
    }

}