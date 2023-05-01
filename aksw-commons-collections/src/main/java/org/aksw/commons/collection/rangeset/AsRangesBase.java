package org.aksw.commons.collection.rangeset;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;

public class AsRangesBase<T extends Comparable<T>>
    extends AbstractSet<Range<T>> {

    protected Set<Range<T>> aset;
    protected Set<Range<T>> bset;
    protected Comparator<Range<T>> endpointComparator;

    public AsRangesBase(
            Set<Range<T>> aset,
            Set<Range<T>> bset,
            Comparator<Range<T>> endpointComparator) {
        this.aset = aset;
        this.bset = bset;
        this.endpointComparator = endpointComparator;
    }

//		@Override
//		public boolean contains(Object o) {
//			boolean r;
//			if (o instanceof Range) {
//				Range<T> range = (Range<T>)o;
//				first.
//
//
//			} else {
//				r = false;
//			}
//
//			return result;
//		}

    @Override
    public Iterator<Range<T>> iterator() {
        return new AbstractIterator<Range<T>>() {

            Iterator<Range<T>> ait = aset.iterator();
            Iterator<Range<T>> bit = bset.iterator();

            Range<T> a = ait.hasNext() ? ait.next() : null;
            Range<T> b = bit.hasNext() ? bit.next() : null;

            // Take and merge ranges from ait and bit as long as they are connected
            @Override
            protected Range<T> computeNext() {
                Range<T> r = null;

                loop: while (true) {
                    // Find out whether a or b has the 'lower' lower-endpoint (or higher upper w.r.t. the comparator); -2 means both are null
                    int c = a != null
                            ? (b != null ? endpointComparator.compare(a, b) : -1)
                            : (b != null ? 1 : -2);

                    switch (c) {
                    case -1:
                    case 0:
                        if (r == null) {
                            r = a;
                            a = ait.hasNext() ? ait.next() : null;
                        } else if (r.isConnected(a)) {
                            r = r.span(a);
                            a = ait.hasNext() ? ait.next() : null;
                        } else {
                            break loop;
                        }
                        break;
                    case 1:
                        if (r == null) {
                            r = b;
                            b = bit.hasNext() ? bit.next() : null;
                        } else if (r.isConnected(b)) {
                            r = r.span(b);
                            b = bit.hasNext() ? bit.next() : null;
                        } else {
                            break loop;
                        }
                        break;
                    case -2:
                        break loop;
                    default:
                        throw new RuntimeException("Should not happen");
                    }
                }

                if (r == null) {
                    r = endOfData();
                }
                return r;
            }
        };
    }

    @Override
    public int size() {
        return Iterators.size(iterator());
    }

}