package org.aksw.commons.util.convert;

public abstract class ConvertFunctionBase
    implements ConvertFunctionRaw
{
    protected Class<?> from;
    protected Class<?> to;

    public ConvertFunctionBase(Class<?> from, Class<?> to) {
        super();
        this.from = from;
        this.to = to;
    }

    @Override
    public Class<?> getFrom() {
        return from;
    }

    @Override
    public Class<?> getTo() {
        return to;
    }
}
