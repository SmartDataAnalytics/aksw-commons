package org.aksw.commons.collections.trees;

/**
 * Base implementation that lazily caches the node count
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class TreeBase<T>
	implements Tree<T>
{
	long nodeCount = -1;

	public long nodeCount() {
		if(nodeCount < 0) {
			nodeCount = TreeUtils.nodeCount(this);
		}

		return nodeCount;
	}
}
