package org.aksw.commons.collections.trees;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tree wrapper with bidirectional replacement of certain nodes with leaf nodes.
 * Useful to replace sub-trees with single nodes.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class TreeReplace<T>
    extends TreeBase<T>
{
    protected Tree<T> delegate;
    //protected BiMap<T, T> delegateToReplacement;
    protected Map<T, T> delegateToReplacement;
    protected Map<T, T> replacementToDelegate;

    public TreeReplace(Tree<T> delegate, Map<T, T> delegateToReplacement, Map<T, T> replacementToDelegate) {
        super();
        this.delegate = delegate;
        this.delegateToReplacement = delegateToReplacement;
        this.replacementToDelegate = replacementToDelegate;
    }

    @Override
    public T getRoot() {
        T b = delegate.getRoot();
        T result = delegateToReplacement.getOrDefault(b, b);
        return result;
    }

    @Override
    public List<T> getChildren(T b) {
        List<T> result;

        if(replacementToDelegate.containsKey(b)) {
            result = Collections.emptyList();
        } else {
            //T a = delegateToReplacement.inverse().getOrDefault(b, b);
            Collection<T> bs = delegate.getChildren(b);
            result = bs.stream().map(bx -> delegateToReplacement.getOrDefault(bx, bx)).collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public T getParent(T b) {
        T a = replacementToDelegate.getOrDefault(b, b);
        T result = delegate.getParent(a);
        return result;
    }

//    public void writeTree(T node, IndentedWriter writer) {
//    	String tmp = "- " + node;
//    	writer.println(tmp);
//    	writer.incIndent();
//    	List<T> children = getChildren(node);
//    	if(children != null) {
//    		for(T child : children) {
//    			writeTree(child, writer);
//    		}
//    	}
//
//    	writer.decIndent();
//    }
//
//	//@Override
//	public String effectiveString() {
//		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//			IndentedWriter writer = new IndentedWriter(out);
//			writer.setPadString("| ");
//			writeTree(getRoot(), writer);
//
//			writer.flush();
//			String result = out.toString(StandardCharsets.UTF_8.name());
//			return result;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

    @Override
    public String toString() {
//		String result = effectiveString();
//		return result;
        return "TreeReplace [delegate=" + delegate + ", delegateToReplacement=" + delegateToReplacement + "]";
    }

    @Override
    public T copy(T node, List<T> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree<T> createNew(T root) {
        throw new UnsupportedOperationException();
    }


}
