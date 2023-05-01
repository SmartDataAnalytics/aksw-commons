package org.aksw.commons.util.range;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import com.google.common.graph.Traverser;


/**
 * Data structure to build a tree from incremental additions of (range, value) pairs.
 *
 * @param <K>
 * @param <V>
 */
public class RangeTreeNode<K extends Comparable<K>, V> {
    protected Range<K> nodeRange;
    protected Collection<V> values;

    protected RangeMap<K, RangeTreeNode<K, V>> children = TreeRangeMap.create(); // = new ArrayList<>();

    public static <K extends Comparable<K>, V> RangeTreeNode<K, V> newRoot() {
        return new RangeTreeNode<K, V>(Range.all());
    }

    public Collection<V> getValues() {
        return values;
    }

    public RangeTreeNode(Range<K> nodeRange) {
        this(nodeRange, Collections.emptySet());
    }

    public RangeTreeNode(Range<K> nodeRange, V value) {
        this(nodeRange, Collections.singleton(value));
    }

    public RangeTreeNode(Range<K> nodeRange, Collection<V> values) {
        super();
        this.nodeRange = nodeRange;
        this.values = new LinkedHashSet<>(values);
    }

    public RangeMap<K, RangeTreeNode<K, V>> getChildren() {
        return children;
    }

    public Collection<RangeTreeNode<K, V>> getChildNodes() {
        return getChildren().asMapOfRanges().values();
    }

    /** Return all values of this node and all of its children - depth first pre order */
    public Stream<V> streamAllValuesPreOrder() {
        return Streams.stream(Traverser.forTree((RangeTreeNode<K, V> node) -> node.getChildNodes()).depthFirstPreOrder(this))
            .flatMap(node -> node.getValues().stream());
    }

    public void put(Range<K> insertRange, V value) {
        Map<Range<K>, RangeTreeNode<K, V>> childMap = children.asMapOfRanges();
        boolean processed = false;

        if (insertRange.equals(nodeRange)) {
            values.add(value);
            processed = true;
        }

        RangeSet<K> overlappingChildren = null;
        if (!processed) {
            // If there is any child node that encloses the insertRange than delegate the insertion to it
            for (Entry<Range<K>, RangeTreeNode<K, V>> child : childMap.entrySet()) {
                Range<K> childRange = child.getKey();
                if (childRange.encloses(insertRange)) {
                    child.getValue().put(insertRange, value);
                    processed = true;
                    break;
                } else if (childRange.isConnected(insertRange) &&  !childRange.intersection(insertRange).isEmpty()) {
                    if (overlappingChildren == null) {
                        overlappingChildren = TreeRangeSet.create();
                    }
                    overlappingChildren.add(childRange);
                }
            }
        }

        // If there were overlapping children then make the insert node a parent of them
        if (!processed && overlappingChildren != null) {
            processed = true;
            // Range<K> newSpan = overlappingChildren.span();
            RangeTreeNode<K, V> newNode = new RangeTreeNode<>(insertRange, value);
            for (Range<K> overlap : overlappingChildren.asRanges()) {
                RangeTreeNode<K, V> node = childMap.remove(overlap);
                newNode.children.put(node.nodeRange, node);
                // children.remove(insertRange);
            }
            children.put(insertRange, newNode);
        }

        // No overlap - just add the new node
        if (!processed) {
            RangeTreeNode<K, V> newNode = new RangeTreeNode<>(insertRange, value);
            children.put(insertRange, newNode);
            processed = true;
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        print(new PrintStream(baos, true, StandardCharsets.UTF_8), "");
        return baos.toString();
    }

    public void print(PrintStream out, String indent) {
        out.println(indent + "span: " + nodeRange);
        out.println(indent + "|- values: " + values);
        int i = 0;
        Iterator<Entry<Range<K>, RangeTreeNode<K, V>>> it = children.asMapOfRanges().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Range<K>, RangeTreeNode<K, V>> entry = it.next();
            out.println(indent + "|- child[" + i + "]");
            String nextIndent = it.hasNext() ? "|  " : "   ";
            entry.getValue().print(out, indent + nextIndent);
            ++i;
        }
    }

    public static void main(String[] args) {

        RangeTreeNode<Integer, String> root = RangeTreeNode.newRoot();

        root.put(Range.closedOpen(0, 5), "a");
        root.put(Range.closedOpen(1, 4), "b");
        root.put(Range.closedOpen(2, 6), "c");
        root.put(Range.closedOpen(10, 20), "d");
        root.put(Range.closedOpen(0, 100), "e");
        root.put(Range.all(), "f");
        System.out.println(root);

    }
}

