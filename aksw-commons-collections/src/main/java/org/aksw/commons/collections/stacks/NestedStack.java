package org.aksw.commons.collections.stacks;

public class NestedStack<T>
    extends GenericNestedStack<T, NestedStack<T>>
{
    public NestedStack(NestedStack<T> parent, T value) {
        super(parent, value);
    }
}
