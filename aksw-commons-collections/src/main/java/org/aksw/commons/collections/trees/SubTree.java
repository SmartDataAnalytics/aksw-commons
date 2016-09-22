package org.aksw.commons.collections.trees;

import java.util.List;
import java.util.function.BiPredicate;


public class SubTree<T>
	extends TreeBase<T>
{
	protected T root;
	protected Tree<T> delegate;
	protected BiPredicate<T, T> nodeComparator;

	public SubTree(Tree<T> delegate, T root) {
		this(delegate, root, (a, b) -> a == b);
	}

	public SubTree(Tree<T> delegate, T root, BiPredicate<T, T> nodeComparator) {
		super();
		this.root = root;
		this.delegate = delegate;
		this.nodeComparator = nodeComparator;
	}

	@Override
	public T getRoot() {
		return root;
	}

	@Override
	public List<T> getChildren(T node) {
		List<T> result = delegate.getChildren(node);
		return result;
	}

	@Override
	public T getParent(T node) {
		boolean isRoot = nodeComparator.test(node, root);
		T result = isRoot ? null : delegate.getParent(node);
		return result;
	}

	@Override
	public T copy(T node, List<T> children) {
		T result = delegate.copy(node, children);
		return result;
	}

	@Override
	public Tree<T> createNew(T root) {
		Tree<T> result = delegate.createNew(root);
		return result;
	}
}
