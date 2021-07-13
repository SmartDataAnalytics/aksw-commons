package org.aksw.commons.util.ref;

/** Interface with default methods that delegate Ref's methods */
public interface RefDelegate<T, R extends Ref<T>>
    extends Ref<T>
{
    R getDelegate();

    @Override
    default Ref<T> getRootRef() {
        return getDelegate().getRootRef();
    }

    @Override
    default T get() {
        return getDelegate().get();
    }

    @Override
    default Ref<T> acquire(Object purpose) {
        return getDelegate().acquire(purpose);
    }

    @Override
    default boolean isAlive() {
        return getDelegate().isAlive();
    }

    @Override
    default boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default Object getSynchronizer() {
        return getDelegate().getSynchronizer();
    }

    @Override
    default StackTraceElement[] getAquisitionStackTrace() {
        return getDelegate().getAquisitionStackTrace();
    }

}