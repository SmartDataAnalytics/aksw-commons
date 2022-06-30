package org.aksw.commons.util.closeable;

/** Interface typically used for removing listener registrations */
public interface Disposable
    extends AutoCloseable
{
    @Override
    void close();
}
