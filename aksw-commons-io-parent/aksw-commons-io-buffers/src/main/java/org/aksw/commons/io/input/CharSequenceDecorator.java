package org.aksw.commons.io.input;

public class CharSequenceDecorator
    implements CharSequence
{
    protected CharSequence decoratee;

    public CharSequenceDecorator(CharSequence decoratee) {
        super();
        this.decoratee = decoratee;
    }

    @Override
    public char charAt(int index) {
        return decoratee.charAt(index);
    }

    @Override
    public int length() {
        return decoratee.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return decoratee.subSequence(start, end);
    }
}
