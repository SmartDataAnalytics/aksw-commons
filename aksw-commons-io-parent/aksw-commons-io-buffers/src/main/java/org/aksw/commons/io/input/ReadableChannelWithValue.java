package org.aksw.commons.io.input;

public class ReadableChannelWithValue<A, T, X extends ReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
{
    protected T value;

    public ReadableChannelWithValue(X delegate) {
        this(delegate, null);
    }

    public ReadableChannelWithValue(X delegate, T value) {
        super(delegate);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public X getDecoratee() {
        return decoratee;
    }
}
