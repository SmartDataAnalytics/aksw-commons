package org.aksw.commons.util.closeable;

public class AutoCloseableDecoratorBase<T extends AutoCloseable>
    implements AutoCloseable
{
    protected T decoratee;

    public AutoCloseableDecoratorBase(T decoratee) {
        super();
        this.decoratee = decoratee;
    }

    protected T getDecoratee() {
        return decoratee;
    }

    @Override
    public void close() throws Exception {
        if (decoratee != null) {
             decoratee.close();
        }
    }
}
