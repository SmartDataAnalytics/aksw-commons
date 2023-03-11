package org.aksw.commons.util.range;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;


/**
 * Data structure to build a tree from incremental additions of (range, value) pairs.
 *
 * @param <K>
 * @param <V>
 */
public class RangeNode<K extends Comparable<K>, V> {
    protected Range<K> nodeRange;
    protected Collection<V> values;

    protected RangeMap<K, RangeNode<K, V>> children = TreeRangeMap.create(); // = new ArrayList<>();

    public static <K extends Comparable<K>, V> RangeNode<K, V> newRoot() {
        return new RangeNode<K, V>(Range.all());
    }

    public RangeNode(Range<K> nodeRange) {
        this(nodeRange, Collections.emptySet());
    }

    public RangeNode(Range<K> nodeRange, V value) {
        this(nodeRange, Collections.singleton(value));
    }

    public RangeNode(Range<K> nodeRange, Collection<V> values) {
        super();
        this.nodeRange = nodeRange;
        this.values = new ArrayList<>(values);
    }

    public void put(Range<K> insertRange, V value) {
        Map<Range<K>, RangeNode<K, V>> childMap = children.asMapOfRanges();
        boolean processed = false;

        if (insertRange.equals(nodeRange)) {
            values.add(value);
            processed = true;
        }

        RangeSet<K> overlappingChildren = null;
        if (!processed) {
            // If there is any child node that encloses the insertRange than delegate the insertion to it
            for (Entry<Range<K>, RangeNode<K, V>> child : childMap.entrySet()) {
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
            RangeNode<K, V> newNode = new RangeNode<>(insertRange, value);
            for (Range<K> overlap : overlappingChildren.asRanges()) {
                RangeNode<K, V> node = childMap.remove(overlap);
                newNode.children.put(node.nodeRange, node);
                // children.remove(insertRange);
            }
            children.put(insertRange, newNode);
        }

        // No overlap - just add the new node
        if (!processed) {
            RangeNode<K, V> newNode = new RangeNode<>(insertRange, value);
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
        Iterator<Entry<Range<K>, RangeNode<K, V>>> it = children.asMapOfRanges().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Range<K>, RangeNode<K, V>> entry = it.next();
            out.println(indent + "|- child[" + i + "]");
            String nextIndent = it.hasNext() ? "|  " : "   ";
            entry.getValue().print(out, indent + nextIndent);
            ++i;
        }
    }

    public static void main(String[] args) {

        RangeNode<Integer, String> root = RangeNode.newRoot();

        root.put(Range.closedOpen(0, 5), "a");
        root.put(Range.closedOpen(1, 4), "b");
        root.put(Range.closedOpen(2, 6), "c");
        root.put(Range.closedOpen(10, 20), "d");
        root.put(Range.closedOpen(0, 100), "e");
        root.put(Range.all(), "f");
        System.out.println(root);

    }
}

