package org.aksw.commons.collections.trees;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class that groups several functions for tree traversal and modification.
 * 
 * @author raven
 *
 * @param <T>
 */
public class TreeOps<T>
{
    protected Function<T, List<T>> parentToChildren;
    protected Function<T, T> childToParent;
    protected BiFunction<T, List<T>, T> replaceChildren;

    public TreeOps(Function<T, List<T>> parentToChildren, Function<T, T> childToParent, BiFunction<T, List<T>, T> replaceChildren) {
        super();
        this.parentToChildren = parentToChildren;
        this.childToParent = childToParent;
        this.replaceChildren = replaceChildren;
    }

	public Function<T, List<T>> getParentToChildren() {
		return parentToChildren;
	}

	public Function<T, T> getChildToParent() {
		return childToParent;
	}

	public BiFunction<T, List<T>, T> getReplaceChildren() {
		return replaceChildren;
	}

	@Override
	public String toString() {
		return "TreeOps [parentToChildren=" + parentToChildren + ", childToParent=" + childToParent
				+ ", replaceChildren=" + replaceChildren + "]";
	}	
}
