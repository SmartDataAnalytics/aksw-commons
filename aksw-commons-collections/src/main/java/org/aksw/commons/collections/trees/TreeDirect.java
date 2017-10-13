package org.aksw.commons.collections.trees;

import java.util.Collection;
import java.util.List;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleSetMultimap;

public class TreeDirect<T>
	implements Tree<T>
{
	protected T root;
	protected ReversibleMap<T, T> nodeToParent;
	
	public TreeDirect(T root, ReversibleMap<T, T> nodeToParent) {
		super();
		this.root = root;
		this.nodeToParent = nodeToParent;
	}

	@Override
	public T getRoot() {
		return root;
	}

	@Override
	public Collection<T> getChildren(T node) {
		ReversibleSetMultimap<T, T> parentToChildren = nodeToParent.reverse();
		Collection<T> result = parentToChildren.get(node);
		return result;
	}

	@Override
	public T getParent(T node) {
		T result = nodeToParent.get(node);
		return result;
	}

	@Override
	public T copy(T node, List<T> children) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tree<T> createNew(T root) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long nodeCount() {
		long result = nodeToParent.size() + 1;
		return result;
	}

}
