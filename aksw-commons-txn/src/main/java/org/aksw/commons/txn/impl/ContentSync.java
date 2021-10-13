package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.function.Consumer;

public interface ContentSync
    extends TxnComponent
{

    InputStream openCurrentContent() throws IOException;

    OutputStream newOutputStreamToNewTmpContent() throws IOException;

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

    /**
     * Replace the new content with the current temporary content
     * @throws IOException
     */
    void preCommit() throws IOException;

    void finalizeCommit() throws IOException;

    void rollback() throws IOException;

}